package com.elacart.SessionController;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Type to represent a detected session 
 */
class Session{
  
  /**
   * Session start time in millisecond
   */
  long sessionStartTime;
  public long getSessionStartTime() {
    return sessionStartTime;
  }
  public void setSessionStartTime(long sessionStartTime) {
    this.sessionStartTime = sessionStartTime;
  }
  public long getSessionEndTime() {
    return sessionEndTime;
  }
  public void setSessionEndTime(long sessionEndTime) {
    this.sessionEndTime = sessionEndTime;
  }
  
  /**
   * Session end time in millisecond
   */
  long sessionEndTime;
}

/**
 * Base class to represent an event 
 */
class Event{
  public static final int SWIPE_TIME_OUT = 20;
  public static final int TOUCH_TIME_OUT = 10;
  
  /**
   * time at which event was fired
   */
  long timeStamp;
  
  /**
   * remaining time before the event is timed out
   */
  int timeout;
  
  /**
   * name of event
   */
  String label;
}

/**
 * Swipe event type, send whenever a swipe is performed (usually by hostess)
 */
class SwipeEvent extends Event{

  public SwipeEvent() {
    super();
    timeout = Event.SWIPE_TIME_OUT;
    // TODO Auto-generated constructor stub
  }
  
}
/**
 * event corresponding to a touch on the screen 
 * @author Sean Yin
 *
 */
class TouchEvent extends Event{
  public TouchEvent() {
    super();
    timeout = Event.TOUCH_TIME_OUT;
    // TODO Auto-generated constructor stub
  }
  
}

/**
 * Either a open or close check event
 * @author Sean
 *
 */
class PayEvent extends Event{
  boolean openEvent;

  public PayEvent() {
    super();
    timeout = Event.TOUCH_TIME_OUT;
    // TODO Auto-generated constructor stub
  }

  public boolean isOpenEvent() {
    return openEvent;
  }

  public void setOpenEvent(boolean openEvent) {
    this.openEvent = openEvent;
  }
}

/**
 * This is the sessioncontroller object, it exposes a procEvent interface
 * to be called by event dispatcher (or test harness in our case) by passing in
 * an single event at a time
 * @author Sean
 *
 */
public class SessionController {
  /**
   * constant interval between the execution of workerthread that update each event's
   * the remaining time before timeout  
   */
  public static final int CHECK_INTERVAL = 2;
  
  /**
   * constructor, init an arraylist of sessions and a event stack, both are empty
   */
  public SessionController() {
    super();
    sessions = new ArrayList<Session>();
    events = new Stack<Event>();
    // TODO Auto-generated constructor stub
  }

  /**
   * nested class that is a runnable running in its own context periodically, for every CHECK_INTERVAL seconds
   * it will go through all the current events to update the remaining time before event timeout, it also
   * checkes to see whether all the events have timed out, in that case, it will call endOfSession method
   * to close the current session and save it into the session list.
   * 
   * 
   * @author Sean
   *
   */
  static class WorkerThread implements Runnable {
    /**
     * if a open check event is registered, this runnable will not update timeout field, untill
     * a close check event is registered.
     */
    static boolean pauseTimer = false; 
    int liveCount;
    public void run() {
      if(pauseTimer){
        //System.out.println("timer paused");
        return;
      }
      liveCount = 0;
      
      //iterating through event stack to update each event's timeout field
      events.forEach((evt)->{
        int aTimeout = evt.timeout-CHECK_INTERVAL;
        evt.timeout = aTimeout<0 ? 0 : aTimeout;
        System.out.println(evt.timeout);
        if(evt.timeout>0){
          liveCount++;
        }
      });
      if(0==liveCount){
        SessionController.endOfSession();
      }
    }
  }
  
  /**
   * a list to store all the sessions that have been detected so far
   */
  static ArrayList<Session> sessions;
  
  /**
   * a list of events that has been received and belong to the current session,
   * using a Java Stack to keep the events to track the order of events, also Java
   * Stack is synchronized, so it is threadsafe to be accessed both on main thread
   * and WorkerThread
   */
  static Stack<Event> events;
  
  static ScheduledThreadPoolExecutor executorPool = new ScheduledThreadPoolExecutor(1);
  static ScheduledFuture<?> task;
  
  /**
   * called when end of session is detected to create a new session
   * and populate it's relevant fields
   */
  public static void endOfSession(){
    Session aSession = new Session();
    Event endEvt = events.pop();
    Event startEvt = null;
    System.out.println(endEvt.label+","+endEvt.timeStamp+","+endEvt.timeout);
    aSession.setSessionEndTime(endEvt.timeStamp);
    while(!events.isEmpty()){
      startEvt = events.pop();
      System.out.println(startEvt.label+","+startEvt.timeStamp+","+startEvt.timeout);
    }
    if(startEvt!=null){
      aSession.setSessionStartTime(startEvt.timeStamp);
    }
    else{
      aSession.setSessionStartTime(aSession.getSessionEndTime());
      aSession.setSessionEndTime(System.currentTimeMillis());
    }
    sessions.add(aSession);
    task.cancel(false);
  }
  
  /**
   * return a list of sessions that this controller has recorded
   * @return ArrayList<Session>
   */
  public ArrayList<Session> getSessions() {
    return sessions;
  }
  
  /**
   * this is the main interface of the controller that will be called by event dispatcher or
   * test harness.
   * Upon receiving an event, it first check if the event stack is empty, if so, it will start a workerthread to 
   * monitor and update event timeout
   * if not, it will check if it is a PayEvent, if so, it will pause or resume the workerthread monitoring
   * based on whether this is a open or close check event.
   * else, it will simply push the event to the top of stack
   * @param evt
   */
  public void procEvent(Event evt){
    evt.timeStamp = System.currentTimeMillis();
    if(events.isEmpty()){
      // received the first event of a session, start a workerthread to monitor and update the events
      task = executorPool.scheduleAtFixedRate(new WorkerThread(), CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }
    if(evt instanceof PayEvent){
      if(((PayEvent) evt).isOpenEvent()){
        // whenever receives a open check event, pause the workerthread from updating the events
        WorkerThread.pauseTimer = true;
      }
      else{
        // close check case, reenable the workerthread to update the events
        WorkerThread.pauseTimer = false;
      }
    }
    else{
        events.push(evt);
    }
  }
 
  
}
