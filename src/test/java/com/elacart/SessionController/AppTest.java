package com.elacart.SessionController;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;


/**
 * Unit test for simple App.
 */
public class AppTest 
{
  SessionController sessionController;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
  @Before
  public void beforeEachTest() {
    sessionController = new SessionController();
  }

    /**
     * @return the suite of tests being tested
     */
   

    /**
     * Test case for two touch events where the second sent before the first event timeout,
     * which yield a single session
     * @throws InterruptedException 
     */
    @Test
    public void testApp() throws InterruptedException
    {
      System.out.println("Enter testApp");
      TouchEvent tEvt = new TouchEvent();
      tEvt.label = "touch1";
      sessionController.procEvent(tEvt);
      Thread.sleep(9000);
      tEvt = new TouchEvent();
      tEvt.label = "touch2";
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      ArrayList<Session> sessions = sessionController.getSessions();
      System.out.println("How many sessions: "+sessions.size());
      assertEquals(1,sessions.size());
      System.out.println("session start time: "+sessions.get(0).getSessionStartTime());
      System.out.println("session end time: "+sessions.get(0).getSessionEndTime());
      long elapse = sessions.get(0).getSessionEndTime() - sessions.get(0).getSessionStartTime();
      System.out.println("elapse: "+elapse);
      assertTrue(Math.abs(elapse-9000) < 20 );
    }
    
    /**
     * Rigourous Test :-)
     * @throws InterruptedException 
     */
    //@Test
    public void test2Sessions() throws InterruptedException
    {
      System.out.println("Enter test2Sessions");
      TouchEvent tEvt = new TouchEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      tEvt = new TouchEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      ArrayList<Session> sessions = sessionController.getSessions();
      System.out.println("How many sessions: "+sessions.size());
      assertEquals(2,sessions.size());
    }
    
    /**
     * Rigourous Test :-)
     * @throws InterruptedException 
     */
    //@Test
    public void test1SessionsWSwipe() throws InterruptedException
    {
      Event tEvt = new SwipeEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      tEvt = new TouchEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      ArrayList<Session> sessions = sessionController.getSessions();
      System.out.println("How many sessions: "+sessions.size());
      assertEquals(1,sessions.size());
    }
    
    /**
     * Case of single touch event followed by a open check and close check event
     * @throws InterruptedException 
     */
    @Test
    public void test1SessionsOpenCheck() throws InterruptedException
    {
      System.out.println("Enter test1SessionsOpenCheck");
      Event tEvt = new TouchEvent();
      tEvt.label = "touch1";
      sessionController.procEvent(tEvt);
      Thread.sleep(9000);
      tEvt = new PayEvent();
      ((PayEvent)tEvt).setOpenEvent(true);
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      tEvt = new PayEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(2000);
      ArrayList<Session> sessions = sessionController.getSessions();
      System.out.println("How many sessions: "+sessions.size());
      long elapse = sessions.get(0).getSessionEndTime() - sessions.get(0).getSessionStartTime();
      System.out.println("elapse: "+elapse);
      assertTrue(Math.abs(elapse-22000) < 20 );
      assertEquals(1,sessions.size());
    }
    
    /**
     * Case of single touch event followed by a open check, a touch event then a close check event
     * then check for results before all events have timeout, yield zero session
     * @throws InterruptedException 
     */
    @Test
    public void test0SessionsMoreDelay() throws InterruptedException
    {
      System.out.println("Enter test1SessionsMoreDelay");
      Event tEvt = new TouchEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(9000);
      tEvt = new PayEvent();
      ((PayEvent)tEvt).setOpenEvent(true);
      sessionController.procEvent(tEvt);
      Thread.sleep(11000);
      tEvt = new TouchEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(2000);
      tEvt = new PayEvent();
      sessionController.procEvent(tEvt);
      Thread.sleep(2000);
      ArrayList<Session> sessions = sessionController.getSessions();
      System.out.println("How many sessions: "+sessions.size());
      assertEquals(0,sessions.size());
    }
}
