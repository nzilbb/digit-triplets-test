//
// (c) 2010-2011, Robert Fromont - robert@fromont.net.nz
//
//    This module is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This module is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this module; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package nz.ac.canterbury.nzilbb.digittripletstest;

import java.applet.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javazoom.jl.player.*; // jl020.jar
import javazoom.jl.decoder.JavaLayerException;
import netscape.javascript.JSObject;
import netscape.javascript.JSException;

/**
 * Applet that implements the Digit Triplets hearing test.  An MP3 is played of a voice saying something like "the digits 3 5 7".  The user presses the digits they think they heard, and a further three digits are played.  The audio played each time depends on whether the last response was correct - if it was correct, more 'background' sound is introduced, if not, a less noisy recording is played.
 * @author Robert Fromont robert@fromont.net.nz
 */
public class DigitTripletsTest
   extends Applet
   implements ActionListener, WindowListener, KeyListener
{
   private static final long serialVersionUID = 1;

   /** Possible value for {@link #iResultCode}: -2 */
   public static final int RESULT_INCOMPLETE = -2;
   /** Possible value for {@link #iResultCode}: -1 */
   public static final int RESULT_POOR = -1;
   /** Possible value for {@link #iResultCode}: 0 */
   public static final int RESULT_INCONCLUSIVE = 0;
   /** Possible value for {@link #iResultCode}: 1 */
   public static final int RESULT_NORMAL = 1;

   private Frame frame_;

   /**
    * Main entrypoint if run as an application
    */
   public static void main(String argv[])
   {
      DigitTripletsTest application = new DigitTripletsTest();
      application.frame_ = new Frame("Digit Triplets Test");
      application.frame_.addWindowListener(application);

      // Load default settings
      Properties settings = new Properties();
      URL settingsUrl = application.getClass().getResource(
	 application.getClass().getSimpleName() + ".properties");
      if (settingsUrl != null)
      {
	 try
	 {
	    URLConnection cnxn = settingsUrl.openConnection();
	    InputStream is = cnxn.getInputStream();
	    settings.load(is);
	    is.close();
	 }
	 catch(Exception exception)
	 {
	    System.err.println(exception.toString());
	 }
      }

      // arguments
      Vector vArguments = new Vector();
      for (int i = 0; i < argv.length; i++)
      {
	 String sArg = argv[i];
	 if (sArg.startsWith("-"))
	 { // switch
	    sArg = sArg.substring(1);
	    int iEquals = sArg.indexOf('=');
	    if (iEquals < 0)
	    {
	       settings.setProperty(sArg.toLowerCase(), "true");
	    }
	    else
	    {
	       settings.setProperty(
		  sArg.substring(0, iEquals).toLowerCase(), 
		  sArg.substring(iEquals + 1));
	    }
	 }
	 else
	 { // argument
	    vArguments.add(sArg);
	 }
      } // next argument
      Vector vMessages = application.loadSettings(settings);
      for (int i = 0; i < vMessages.size(); i++)
      {
	 System.err.println(vMessages.elementAt(i));
      }
      if (settings.getProperty("usage") != null)
      {
	 System.out.println("usage:");
	 System.out.println("java " + application.getClass().getName() + " -mediaurlbase=<URL> -volumecheckfile=<file> -trials0=<digits-list> -trials1=<digits-list> ...");
	 System.out.println("switches:");
	 System.out.println("\t-usage\tprint this usage information");
	 System.out.println("\t-mediaurlbase=<URL>\tThe location of media files");
	 System.out.println("\t-volumecheckfile=<fileName>\tThe name of the file to use for checking volume");
	 System.out.println("\t-trials<n>=<digits-list>\tAn ordered, comma-separated list of digit triplets e.g. 157,349,285,796,265");
	 System.out.println("\t-filenameformat=<format>\tThe format for the media files - e.g. TheDigits_{0}_{1}_dBSNR.mp3");
	 System.out.println("\t-soundlevelstart=<level>\tThe starting sound level");
	 System.out.println("\t-soundlevelmaximum=<level>\tThe maximum sound level");
	 System.out.println("\t-stopwhensignaltoohigh={true|false}\tWhether to stop the test when the signal level goes above the maximum or to continue the test at the maximum signal level");
	 System.out.println("\t-soundlevelminimum=<level>\tThe minimum sound level");
	 System.out.println("\t-soundlevelincrement=<inc>\tBy how much the sound level is increased/decreased between each trial");
	 System.out.println("\t-snrpoormin=<inc>\tMinimum mean SNR for result to be considered 'poor'");
	 System.out.println("\t-snrnormalmax=<inc>\tMaximum mean SNR for the result to be considered 'normal'");
	 System.out.println("\t-timeoutseconds=<s>\tNumber of seconds to wait before timing-out a trial and allowing the next trial to start");
	 System.out.println("\t-compact={true|false}\tWhether to use the compact (2 button) layout or not");
	 return;
      }

      application.init();
      application.start();

      Toolkit toolkit = application.frame_.getToolkit();
      Dimension screenSize = toolkit.getScreenSize();
      int width = application.getCompact()? 300 : 500;
      int height = application.getCompact()? 150 : 400;
      int top = (screenSize.height - height) / 2;
      int left = (screenSize.width - width) / 2;
      // icon
      try
      {
	 URL imageUrl = application.getClass().getResource(
	    application.getClass().getSimpleName() + ".png");
	 if (imageUrl != null)
	 {
	    application.frame_.setIconImage(toolkit.createImage(imageUrl));
	 }
      }
      catch(Exception exception)
      {}
      application.frame_.add("Center", application);
      application.frame_.setSize(width, height);
      application.frame_.setLocation(left, top);
      application.frame_.setVisible(true);
   } // end of main

   // Attributes:

   /** Message display */
   protected JTextField lblMessage = new JTextField(" "); //new JLabel(" ", SwingConstants.CENTER);
   /** Play button */
   protected JButton btnPlay = new JButton("Start");
   /** Digit button */
   protected JButton btn0 = new JButton("0");
   /** Digit button */
   protected JButton btn1 = new JButton("1");
   /** Digit button */
   protected JButton btn2 = new JButton("2");
   /** Digit button */
   protected JButton btn3 = new JButton("3");
   /** Digit button */
   protected JButton btn4 = new JButton("4");
   /** Digit button */
   protected JButton btn5 = new JButton("5");
   /** Digit button */
   protected JButton btn6 = new JButton("6");
   /** Digit button */
   protected JButton btn7 = new JButton("7");
   /** Digit button */
   protected JButton btn8 = new JButton("8");
   /** Digit button */
   protected JButton btn9 = new JButton("9");
   /** Clear button */
   protected JButton btnClear = new JButton("Clear");

   /** Volume check button */
   protected JButton btnVolumeCheck = new JButton("Check Volume");

   /** Volume check player */
   protected Player volumeCheckPlayer = null;

   /** Volume check data */
   protected byte[] volumeCheck = null;

   /** Progress bar */
   protected JProgressBar pb = new JProgressBar();

   /** Ordered list of triplets (Trial) */
   protected Vector vTriplets = new Vector();

   /** Current trial */
   protected int iTrial = 0;

   /** Trial for which audio is available */
   protected int iPreparedTrial = 0;

   /** Audio device */
   AudioDevice device;

   /** Media player for this trial */
   protected Player player = null;
      
   /** Media for next trial - noisier version */
   protected byte[] nextMediaNoisy = null;

   /** Media for next trial - less noisy version */
   protected byte[] nextMediaQuiet = null;

   /** Next trial signal level - noisier version */
   protected int iSignalLevelNoisy;

   /** Next trial signal level - less noisy version */
   protected int iSignalLevelQuiet;

   /** Play icon */
   protected ImageIcon icoPlay;

   /** Playing icon */
   protected ImageIcon icoPlaying;

   /** Playing/waiting thread */
   protected Thread playThread;

   /**
    * The format of the media filenames.
    * <br> e.g. "TheDigits_{0}_{1}_dBSNR.mp3" where
    * <ul>
    *  <li>{0} = digit triplets</li>
    *  <li>{1} = SNR </li>
    * </ul>
    * @see #getFileNameFormat()
    * @see #setFileNameFormat(MessageFormat)
    */
   protected MessageFormat fmtFileNameFormat;
   /**
    * Getter for {@link #fmtFileNameFormat}: The format of the media filenames.
    * <br> e.g. "TheDigits_{0}_{1}_dBSNR.mp3" where
    * <ul>
    *  <li>{0} = digit triplets</li>
    *  <li>{1} = SNR </li>
    * </ul>
    * @return The format of the filename.
    */
   public MessageFormat getFileNameFormat() { return fmtFileNameFormat; }
   /**
    * Setter for {@link #fmtFileNameFormat}: The format of the media filenames.
    * <br> e.g. "TheDigits_{0}_{1}_dBSNR.mp3" where
    * <ul>
    *  <li>{0} = digit triplets</li>
    *  <li>{1} = SNR </li>
    * </ul>
    * @param fmtNewFileNameFormat The format of the filename.
    */
   public void setFileNameFormat(MessageFormat fmtNewFileNameFormat) { fmtFileNameFormat = fmtNewFileNameFormat; }


   /**
    * A list of comma-separated lists of digit-triplets in order
    * @see #getTrials()
    * @see #setTrials(Vector)
    */
   protected Vector vTrials = new Vector();
   /**
    * Getter for {@link #vTrials}: A list of comma-separated lists of digit-triplets in order
    * @return A list of comma-separated lists of digit-triplets in order
    */
   public Vector getTrials() { return vTrials; }
   /**
    * Setter for {@link #vTrials}: A list of comma-separated lists of digit-triplets in order
    * @param sNewTrials A list of comma-separated lists of digit-triplets in order
    */
   public void setTrials(Vector vNewTrials) 
   { 
      vTrials = vNewTrials; 
      getTrialTokens();
   }


   /**
    * Number of the randomly chosen trial set
    * @see #getTrialSetNumber()
    * @see #setTrialSetNumber(int)
    */
   protected int iTrialSetNumber;
   /**
    * Getter for {@link #iTrialSetNumber}: Number of the randomly chosen trial set
    * @return Number of the randomly chosen trial set
    */
   public int getTrialSetNumber() { return iTrialSetNumber; }
   /**
    * Setter for {@link #iTrialSetNumber}: Number of the randomly chosen trial set
    * @param iNewTrialSetNumber Number of the randomly chosen trial set
    */
   public void setTrialSetNumber(int iNewTrialSetNumber) { iTrialSetNumber = iNewTrialSetNumber; }


   /**
    * Which set of trials to use
    * @see #getTrialSet()
    * @see #setTrialSet(int)
    */
   protected int iTrialSet = 0;
   /**
    * Getter for {@link #iTrialSet}: Which set of trials to use
    * @return Which set of trials to use
    */
   public int getTrialSet() { return iTrialSet; }
   /**
    * Setter for {@link #iTrialSet}: Which set of trials to use
    * @param iNewTrialSet Which set of trials to use
    */
   public void setTrialSet(int iNewTrialSet) { iTrialSet = iNewTrialSet; }

      
   /**
    * The location of media files
    * @see #getMediaUrlBase()
    * @see #setMediaUrlBase(URL)
    */
   protected URL urlMediaUrlBase;
   /**
    * Getter for {@link #urlMediaUrlBase}: The location of media files
    * @return The location of media files
    */
   public URL getMediaUrlBase() { return urlMediaUrlBase; }
   /**
    * Setter for {@link #urlMediaUrlBase}: The location of media files
    * @param urlNewMediaUrlBase The location of media files
    */
   public void setMediaUrlBase(URL urlNewMediaUrlBase) { urlMediaUrlBase = urlNewMediaUrlBase; }

      
   /**
    * Name of media file to use for checking volume
    * @see #getVolumCheckFile()
    * @see #setVolumCheckFile(String)
    */
   protected String sVolumCheckFile;
   /**
    * Getter for {@link #sVolumCheckFile}: Name of media file to use for checking volume
    * @return Name of media file to use for checking volume
    */
   public String getVolumCheckFile() { return sVolumCheckFile; }
   /**
    * Setter for {@link #sVolumCheckFile}: Name of media file to use for checking volume
    * @param sNewVolumCheckFile Name of media file to use for checking volume
    */
   public void setVolumCheckFile(String sNewVolumCheckFile) { sVolumCheckFile = sNewVolumCheckFile; }


   /**
    * Starting Signal level
    * @see #getSignalLevelStart()
    * @see #setSignalLevelStart(int)
    */
   protected int iSignalLevelStart = 0;
   /**
    * Getter for {@link #iSignalLevelStart}: Starting Signal level
    * @return Starting Signal level
    */
   public int getSignalLevelStart() { return iSignalLevelStart; }
   /**
    * Setter for {@link #iSignalLevelStart}: Starting Signal level
    * @param iNewSignalLevelStart Starting Signal level
    */
   public void setSignalLevelStart(int iNewSignalLevelStart) { iSignalLevelStart = iNewSignalLevelStart; }


   /**
    * Number of decibels the Signal level increments or decrements
    * @see #getSignalLevelIncrement()
    * @see #setSignalLevelIncrement(int)
    */
   protected int iSignalLevelIncrement = 2;
   /**
    * Getter for {@link #iSignalLevelIncrement}: Number of decibels the Signal level increments or decrements
    * @return Number of decibels the Signal level increments or decrements
    */
   public int getSignalLevelIncrement() { return iSignalLevelIncrement; }
   /**
    * Setter for {@link #iSignalLevelIncrement}: Number of decibels the Signal level increments or decrements
    * @param iNewSignalLevelIncrement Number of decibels the Signal level increments or decrements
    */
   public void setSignalLevelIncrement(int iNewSignalLevelIncrement) { iSignalLevelIncrement = iNewSignalLevelIncrement; }

   /**
    * Maximum possible Signal level
    * @see #getSignalLevelMaximum()
    * @see #setSignalLevelMaximum(int)
    */
   protected int iSignalLevelMaximum = 4;
   /**
    * Getter for {@link #iSignalLevelMaximum}: Maximum possible Signal level
    * @return Maximum possible Signal level
    */
   public int getSignalLevelMaximum() { return iSignalLevelMaximum; }
   /**
    * Setter for {@link #iSignalLevelMaximum}: Maximum possible Signal level
    * @param iNewSignalLevelMaximum Maximum possible Signal level
    */
   public void setSignalLevelMaximum(int iNewSignalLevelMaximum) { iSignalLevelMaximum = iNewSignalLevelMaximum; }


      /**
       * Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)
       * @see #getStopWhenSignalTooHigh()
       * @see #setStopWhenSignalTooHigh(boolean)
       */
      protected boolean bStopWhenSignalTooHigh = true;
      /**
       * Getter for {@link #bStopWhenSignalTooHigh}: Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)
       * @return Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)
       */
      public boolean getStopWhenSignalTooHigh() { return bStopWhenSignalTooHigh; }
      /**
       * Setter for {@link #bStopWhenSignalTooHigh}: Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)
       * @param bNewStopWhenSignalTooHigh Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)
       */
      public void setStopWhenSignalTooHigh(boolean bNewStopWhenSignalTooHigh) { bStopWhenSignalTooHigh = bNewStopWhenSignalTooHigh; }


   /**
    * Minimum Signal level
    * @see #getSignalLevelMinimum()
    * @see #setSignalLevelMinimum(int)
    */
   protected int iSignalLevelMinimum = -22;
   /**
    * Getter for {@link #iSignalLevelMinimum}: Minimum Signal level
    * @return Minimum Signal level
    */
   public int getSignalLevelMinimum() { return iSignalLevelMinimum; }
   /**
    * Setter for {@link #iSignalLevelMinimum}: Minimum Signal level
    * @param iNewSignalLevelMinimum Minimum Signal level
    */
   public void setSignalLevelMinimum(int iNewSignalLevelMinimum) { iSignalLevelMinimum = iNewSignalLevelMinimum; }


   /**
    * The number of starting trials to ignore when calculating SNR.
    * @see #getIgnoreTrials()
    * @see #setIgnoreTrials(int)
    */
   protected int iIgnoreTrials;
   /**
    * Getter for {@link #iIgnoreTrials}: The number of starting trials to ignore when calculating SNR.
    * @return The number of starting trials to ignore when calculating SNR.
    */
   public int getIgnoreTrials() { return iIgnoreTrials; }
   /**
    * Setter for {@link #iIgnoreTrials}: The number of starting trials to ignore when calculating SNR.
    * @param iNewIgnoreTrials The number of starting trials to ignore when calculating SNR.
    */
   public void setIgnoreTrials(int iNewIgnoreTrials) { iIgnoreTrials = iNewIgnoreTrials; }

   
   /**
    * CSV representation of trial results so far.
    * @see #getTrialsCsv()
    * @see #setTrialsCsv(String)
    */
   protected String sTrialsCsv;
   /**
    * Getter for {@link #sTrialsCsv}: CSV representation of trial results so far.
    * @return CSV representation of trial results so far.
    */
   public String getTrialsCsv() { return sTrialsCsv; }
   /**
    * Setter for {@link #sTrialsCsv}: CSV representation of trial results so far.
    * @param sNewTrialsCsv CSV representation of trial results so far.
    */
   public void setTrialsCsv(String sNewTrialsCsv) { sTrialsCsv = sNewTrialsCsv; }


   /**
    * The ID of an &lt;input&gt; tag in the applets hosting document where the trial set number should be stored before posting.
    * @see #getTrialNumberFormInputId()
    * @see #setTrialNumberFormInputId(String)
    */
   protected String sTrialSetNumberFormInputId;
   /**
    * Getter for {@link #sTrialNumberFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the trial set number should be stored before posting.
    * @return The ID of an &lt;input&gt; tag in the applets hosting document where the trial set number should be stored before posting.
    */
   public String getTrialSetNumberFormInputId() { return sTrialSetNumberFormInputId; }
   /**
    * Setter for {@link #sTrialNumberFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the trial set number should be stored before posting.
    * @param sNewTrialNumberFormInputId The ID of an &lt;input&gt; tag in the applets hosting document where the trial set number should be stored before posting.
    */
   public void setTrialSetNumberFormInputId(String sNewTrialSetNumberFormInputId) { sTrialSetNumberFormInputId = sNewTrialSetNumberFormInputId; }

   /**
    * The ID of an &lt;input&gt; tag in the applets hosting document where the trials should be stored before posting.
    * @see #getTrialsFormInputId()
    * @see #setTrialsFormInputId(String)
    */
   protected String sTrialsFormInputId;
   /**
    * Getter for {@link #sTrialsFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the trials should be stored before posting.
    * @return The ID of an &lt;input&gt; tag in the applets hosting document where the trials should be stored before posting.
    */
   public String getTrialsFormInputId() { return sTrialsFormInputId; }
   /**
    * Setter for {@link #sTrialsFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the trials should be stored before posting.
    * @param sNewTrialsFormInputId The ID of an &lt;input&gt; tag in the applets hosting document where the trials should be stored before posting.
    */
   public void setTrialsFormInputId(String sNewTrialsFormInputId) { sTrialsFormInputId = sNewTrialsFormInputId; }

      /**
       * The ID of an &lt;input&gt; tag in the applets hosting document where the result of the test should be sotred before posting.
       * @see #getResultFormInputId()
       * @see #setResultFormInputId(String)
       */
      protected String sResultFormInputId;
      /**
       * Getter for {@link #sResultFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the result of the test should be sotred before posting.
       * @return The ID of an &lt;input&gt; tag in the applets hosting document where the result of the test should be sotred before posting.
       */
      public String getResultFormInputId() { return sResultFormInputId; }
      /**
       * Setter for {@link #sResultFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the result of the test should be sotred before posting.
       * @param sNewResultFormInputId The ID of an &lt;input&gt; tag in the applets hosting document where the result of the test should be sotred before posting.
       */
      public void setResultFormInputId(String sNewResultFormInputId) { sResultFormInputId = sNewResultFormInputId; }

      /**
       * The ID of an &lt;input&gt; tag in the applets hosting document where the Mean SNR of the test should be stored before posting.
       * @see #getMeanSNRFormInputId()
       * @see #setMeanSNRFormInputId(String)
       */
      protected String sMeanSNRFormInputId;
      /**
       * Getter for {@link #sMeanSNRFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the Mean SNR of the test should be stored before posting.
       * @return The ID of an &lt;input&gt; tag in the applets hosting document where the Mean SNR of the test should be stored before posting.
       */
      public String getMeanSNRFormInputId() { return sMeanSNRFormInputId; }
      /**
       * Setter for {@link #sMeanSNRFormInputId}: The ID of an &lt;input&gt; tag in the applets hosting document where the Mean SNR of the test should be stored before posting.
       * @param sNewMeanSNRFormInputId The ID of an &lt;input&gt; tag in the applets hosting document where the Mean SNR of the test should be stored before posting.
       */
      public void setMeanSNRFormInputId(String sNewMeanSNRFormInputId) { sMeanSNRFormInputId = sNewMeanSNRFormInputId; }

   /**
    * The ID of a &lt;form&gt; tag in the applets hosting document which should be submitted when the applet finishes the trials.
    * @see #getFormId()
    * @see #setFormId(String)
    */
   protected String sFormId;
   /**
    * Getter for {@link #sFormId}: The ID of a &lt;form&gt; tag in the applets hosting document which should be submitted when the applet finishes the trials.
    * @return The ID of a &lt;form&gt; tag in the applets hosting document which should be submitted when the applet finishes the trials.
    */
   public String getFormId() { return sFormId; }
   /**
    * Setter for {@link #sFormId}: The ID of a &lt;form&gt; tag in the applets hosting document which should be submitted when the applet finishes the trials.
    * @param sNewFormId The ID of a &lt;form&gt; tag in the applets hosting document which should be submitted when the applet finishes the trials.
    */
   public void setFormId(String sNewFormId) { sFormId = sNewFormId; }


      /**
       * The number of seconds to wait before timing-out a trial and allowing the next trial to start.
       * @see #getTimeoutSeconds()
       * @see #setTimeoutSeconds(int)
       */
      protected int iTimeoutSeconds = 20;
      /**
       * Getter for {@link #iTimeoutSeconds}: The number of seconds to wait before timing-out a trial and allowing the next trial to start.
       * @return The number of seconds to wait before timing-out a trial and allowing the next trial to start.
       */
      public int getTimeoutSeconds() { return iTimeoutSeconds; }
      /**
       * Setter for {@link #iTimeoutSeconds}: The number of seconds to wait before timing-out a trial and allowing the next trial to start.
       * @param iNewTimeoutSeconds The number of seconds to wait before timing-out a trial and allowing the next trial to start.
       */
      public void setTimeoutSeconds(int iNewTimeoutSeconds) { iTimeoutSeconds = iNewTimeoutSeconds; }


      /**
       * Threshold for a 'poor' result
       * @see #getSnrPoorMin()
       * @see #setSnrPoorMin(double)
       */
      protected double dSnrPoorMin = -8.52;
      /**
       * Getter for {@link #dSnrPoorMin}: Threshold for a 'poor' result
       * @return Threshold for a 'poor' result
       */
      public double getSnrPoorMin() { return dSnrPoorMin; }
      /**
       * Setter for {@link #dSnrPoorMin}: Threshold for a 'poor' result
       * @param dNewSnrPoorMin Threshold for a 'poor' result
       */
      public void setSnrPoorMin(double dNewSnrPoorMin) { dSnrPoorMin = dNewSnrPoorMin; }


      /**
       * Threshold for a 'normal' result
       * @see #getSnrNormalMax()
       * @see #setSnrNormalMax(double)
       */
      protected double dSnrNormalMax = -10.32;
      /**
       * Getter for {@link #dSnrNormalMax}: Threshold for a 'normal' result
       * @return Threshold for a 'normal' result
       */
      public double getSnrNormalMax() { return dSnrNormalMax; }
      /**
       * Setter for {@link #dSnrNormalMax}: Threshold for a 'normal' result
       * @param dNewSnrNormalMax Threshold for a 'normal' result
       */
      public void setSnrNormalMax(double dNewSnrNormalMax) { dSnrNormalMax = dNewSnrNormalMax; }

   
      /**
       * Result code for the text - one of {@link #RESULT_INCOMPLETE}, {@link #RESULT_POOR}, {@link #RESULT_INCONCLUSIVE}, {@link #RESULT_NORMAL}
       * @see #getResultCode()
       * @see #setResultCode(int)
       */
      protected int iResultCode = RESULT_INCOMPLETE;
      /**
       * Getter for {@link #iResultCode}: Result code for the text - one of {@link #RESULT_INCOMPLETE}, {@link #RESULT_POOR}, {@link #RESULT_INCONCLUSIVE}, {@link #RESULT_NORMAL}
       * @return Result code for the text - one of {@link #RESULT_INCOMPLETE}, {@link #RESULT_POOR}, {@link #RESULT_INCONCLUSIVE}, {@link #RESULT_NORMAL}
       */
      public int getResultCode() { return iResultCode; }
      /**
       * Setter for {@link #iResultCode}: Result code for the text - one of {@link #RESULT_INCOMPLETE}, {@link #RESULT_POOR}, {@link #RESULT_INCONCLUSIVE}, {@link #RESULT_NORMAL}
       * @param iNewResultCode Result code for the text - one of {@link #RESULT_INCOMPLETE}, {@link #RESULT_POOR}, {@link #RESULT_INCONCLUSIVE}, {@link #RESULT_NORMAL}
       */
      public void setResultCode(int iNewResultCode) { iResultCode = iNewResultCode; }


      /**
       * Whether to use the compact (2 button) test or not.
       * @see #getCompact()
       * @see #setCompact(boolean)
       */
      protected boolean bCompact = false;
      /**
       * Getter for {@link #bCompact}: Whether to use the compact (2 button) test or not.
       * @return Whether to use the compact (2 button) test or not.
       */
      public boolean getCompact() { return bCompact; }
      /**
       * Setter for {@link #bCompact}: Whether to use the compact (2 button) test or not.
       * @param bNewCompact Whether to use the compact (2 button) test or not.
       */
      public void setCompact(boolean bNewCompact) { bCompact = bNewCompact; }


   protected String textStart = "Start";
   protected String textClear = "Clear";
   protected String textCheckVolume = "Check Volume";
   protected String textClearAllDigitsEntered = "Clear all digits entered";
   protected String textGoToTheNextStep = "Go to the next step";
   protected String textHowFarThroughTheTestYouAre = "How far through the test you are";
   protected String textProblemLoadingAudio = "Problem loading audio";
   protected String textNext = "Next";
   protected String textPleaseWait = "Please wait...";
   protected String textFinish = "Finish";

   // Methods:

   /**
    * Initialise the applet.
    */
   public void init()
   {
      // ensure fresh starting state
      // iResultCode = RESULT_INCOMPLETE;
      // vTriplets = new Vector();
      // iTrial = 0;
      
      // are we running as an applet?
      if (frame_ == null)
      {
	 // get parameters
	 Properties settings = new Properties();
	 URL settingsUrl = getClass().getResource(
	    getClass().getSimpleName() + ".properties");
	 if (settingsUrl != null)
	 {
	    try
	    {
	       URLConnection cnxn = settingsUrl.openConnection();
	       InputStream is = cnxn.getInputStream();
	       settings.load(is);
	       is.close();
	    }
	    catch(Exception exception)
	    {
	       System.err.println(exception.toString());
	    }
	 }
	 if (getParameter("mediaurlbase") != null)
	 {
	    settings.setProperty("mediaurlbase", getParameter("mediaurlbase"));
	 }
	 if (getParameter("formid") != null)
	 {
	    settings.setProperty(
	       "formid", getParameter("formid"));
	 }
	 if (getParameter("trialsetnumberforminputid") != null)
	 {
	    settings.setProperty(
	       "trialsetnumberforminputid", getParameter("trialsetnumberforminputid"));
	 }
	 if (getParameter("meansnrforminputid") != null)
	 {
	    settings.setProperty(
	       "meansnrforminputid", getParameter("meansnrforminputid"));
	 }
	 if (getParameter("trialsforminputid") != null)
	 {
	    settings.setProperty(
	       "trialsforminputid", getParameter("trialsforminputid"));
	 }
	 if (getParameter("resultforminputid") != null)
	 {
	    settings.setProperty(
	       "resultforminputid", getParameter("resultforminputid"));
	 }
	 if (getParameter("timeoutseconds") != null)
	 {
	    settings.setProperty(
	       "timeoutseconds", getParameter("timeoutseconds"));
	 }
	 if (getParameter("volumecheckfile") != null)
	 {
	    settings.setProperty(
	       "volumecheckfile", getParameter("volumecheckfile"));
	 }
	 if (getParameter("filenameformat") != null)
	 {
	    settings.setProperty(
	       "filenameformat", getParameter("filenameformat"));
	 }
	 if (getParameter("signallevelstart") != null)
	 {
	    settings.setProperty(
	       "signallevelstart", getParameter("signallevelstart"));
	 }
	 if (getParameter("signallevelincrement") != null)
	 {
	    settings.setProperty(
	       "signallevelincrement", getParameter("signallevelincrement"));
	 }
	 if (getParameter("signallevelmaximum") != null)
	 {
	    settings.setProperty(
	       "signallevelmaximum", getParameter("signallevelmaximum"));
	 }
	 if (getParameter("stopwhensignaltoohigh") != null)
	 {
	    settings.setProperty(
	       "stopwhensignaltoohigh", getParameter("stopwhensignaltoohigh"));
	 }
	 if (getParameter("signallevelminimum") != null)
	 {
	    settings.setProperty(
	       "signallevelminimum", getParameter("signallevelminimum"));
	 }
	 if (getParameter("ignoretrials") != null)
	 {
	    settings.setProperty(
	       "ignoretrials", getParameter("ignoretrials"));
	 }
	 if (getParameter("snrpoormin") != null)
	 {
	    settings.setProperty(
	       "snrpoormin", getParameter("snrpoormin"));
	 }
	 if (getParameter("snrnormalmax") != null)
	 {
	    settings.setProperty(
	       "snrnormalmax", getParameter("snrnormalmax"));
	 }
	 if (getParameter("compact") != null)
	 {
	    settings.setProperty(
	       "compact", getParameter("compact"));
	 }
	 int i = 0;
	 while (getParameter("trials" + i) != null)
	 {
	    settings.setProperty("trials" + i, getParameter("trials" + i));
	 } // next trial

	 Vector vMessages = loadSettings(settings);
	 for (int j = 0; j < vMessages.size(); j++)
	 {
	    lblMessage.setText((String)vMessages.elementAt(j));
	    System.err.println((String)vMessages.elementAt(j));
	 }

      }

      // icon
      try
      {
	 URL imageUrl = getClass().getResource("play.png");
	 if (imageUrl != null)
	 {
	    icoPlay = new ImageIcon(imageUrl);
	    btnPlay.setIcon(icoPlay);
	    btnPlay.setHorizontalTextPosition(SwingConstants.LEFT);
	 }
      }
      catch(Exception exception) {}
      try
      {
	 URL imageUrl = getClass().getResource("playing.png");
	 if (imageUrl != null)
	 {
	    icoPlaying = new ImageIcon(imageUrl);
	 }
      }
      catch(Exception exception) {}
      try
      {
	 URL imageUrl = getClass().getResource("clear.png");
	 if (imageUrl != null)
	 {
	    btnClear.setIcon(new ImageIcon(imageUrl));
	    btnClear.setHorizontalTextPosition(SwingConstants.RIGHT);
	 }
      }
      catch(Exception exception) {}

      btnVolumeCheck.setHorizontalTextPosition(SwingConstants.LEFT);

      setLayout(new BorderLayout(6,6));

      add(btnVolumeCheck, BorderLayout.NORTH);
      add(pb, BorderLayout.SOUTH);

      JPanel pnlKeyPad = new JPanel(new GridLayout(4,3, 5,5));
      if (!getCompact())
      {	 
	 pnlKeyPad.add(btn1);
	 pnlKeyPad.add(btn2);
	 pnlKeyPad.add(btn3);
	 pnlKeyPad.add(btn4);
	 pnlKeyPad.add(btn5);
	 pnlKeyPad.add(btn6);
	 pnlKeyPad.add(btn7);
	 pnlKeyPad.add(btn8);
	 pnlKeyPad.add(btn9);
	 pnlKeyPad.add(btnClear);
	 pnlKeyPad.add(btn0);
	 pnlKeyPad.add(btnPlay);
	 
	 // big digits
	 btn1.setFont(new Font("SansSerif", Font.BOLD, 28));
	 btn2.setFont(btn1.getFont());
	 btn3.setFont(btn1.getFont());
	 btn4.setFont(btn1.getFont());
	 btn5.setFont(btn1.getFont());
	 btn6.setFont(btn1.getFont());
	 btn7.setFont(btn1.getFont());
	 btn8.setFont(btn1.getFont());
	 btn9.setFont(btn1.getFont());
	 btn0.setFont(btn1.getFont());
	 
	 btn0.setEnabled(false);
	 btn1.setEnabled(false);
	 btn2.setEnabled(false);
	 btn3.setEnabled(false);
	 btn4.setEnabled(false);
	 btn5.setEnabled(false);
	 btn6.setEnabled(false);
	 btn7.setEnabled(false);
	 btn8.setEnabled(false);
	 btn9.setEnabled(false);
      }
      else
      {
	 pnlKeyPad = new JPanel(new GridLayout(1,2, 5,5));
	 pnlKeyPad.add(btnClear);
	 pnlKeyPad.add(btnPlay);
      }
      add(pnlKeyPad, BorderLayout.CENTER);
      
      btnClear.setFont(new Font("SansSerif", Font.BOLD, 14));
      btnPlay.setFont(btnClear.getFont());
      btnVolumeCheck.setFont(new Font("SansSerif", Font.BOLD, 20));
      
      btnClear.setToolTipText(textClearAllDigitsEntered);
      btnPlay.setToolTipText(textGoToTheNextStep);
      pb.setToolTipText(textHowFarThroughTheTestYouAre);
      btnPlay.setText(textStart);
      btnClear.setText(textClear);
      btnVolumeCheck.setText(textCheckVolume);

      lblMessage.setFont(new Font("SansSerif", Font.BOLD, 28));
      lblMessage.setHorizontalAlignment(JTextField.CENTER);
      lblMessage.setEditable(false);

      btn1.addActionListener(this);
      btn2.addActionListener(this);
      btn3.addActionListener(this);
      btn4.addActionListener(this);
      btn5.addActionListener(this);
      btn6.addActionListener(this);
      btn7.addActionListener(this);
      btn8.addActionListener(this);
      btn9.addActionListener(this);
      btn0.addActionListener(this);
      btnClear.addActionListener(this);
      btnPlay.addActionListener(this);
      btnVolumeCheck.addActionListener(this);

      btnClear.setEnabled(false);

      addKeyListener(this);
      lblMessage.addKeyListener(this);
      btn1.addKeyListener(this);
      btn2.addKeyListener(this);
      btn3.addKeyListener(this);
      btn4.addKeyListener(this);
      btn5.addKeyListener(this);
      btn6.addKeyListener(this);
      btn7.addKeyListener(this);
      btn8.addKeyListener(this);
      btn9.addKeyListener(this);
      btn0.addKeyListener(this);
      btnClear.addKeyListener(this);
      btnPlay.addKeyListener(this);

      pb.setMaximum(vTriplets.size());

      try
      {
	 loadVolumeCheck();
      }
      catch(Throwable t)
      {
	 lblMessage.setText("Problem loading volume check audio: "+t);
	 System.err.println(lblMessage.getText());
      }

   } // end of init()

      
   /**
    * Loads settings from a given Properties object
    * @param settings
    * @return A list of errors
    */
   public Vector loadSettings(Properties settings)
   {
      Vector vMessages = new Vector();
      if (settings.getProperty("mediaurlbase") != null)
      {
	 try
	 {
	    String s = settings.getProperty("mediaurlbase");
	    if (!s.startsWith("http://"))
	    {
	       try
	       {
		  URL url = new URL(getDocumentBase(), s);
		  s = url.toString();
	       }
	       catch(Throwable t)
	       {
		  System.err.println("mediaurlbase: " + t);
	       }
	    }
	    setMediaUrlBase(new URL(s));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid mediaurlbase \"" 
	       + settings.getProperty("mediaurlbase") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","mediaurlbase");
	 }
      }
      else
      {
	 vMessages.addElement("No mediaurlbase supplied");
	 settings.setProperty("usage","mediaurlbase");
      }
      if (settings.getProperty("volumecheckfile") != null)
      {
	 setVolumCheckFile(
	    settings.getProperty("volumecheckfile"));
      }
      else
      {
	 vMessages.addElement("No volume check file supplied");
	 settings.setProperty("usage","volumecheckfile");
      }
      if (settings.getProperty("signallevelmaximum") != null)
      {
	 try
	 {
	    setSignalLevelMaximum(
	       Integer.parseInt(settings.getProperty("signallevelmaximum")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid signallevelmaximum \"" 
	       + settings.getProperty("signallevelmaximum") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","signallevelmaximum");
	 }
      }
      else
      {
	 vMessages.addElement("No signallevelmaximum supplied");
	 settings.setProperty("usage","signallevelmaximum");
      }
      if (settings.getProperty("stopwhensignaltoohigh") != null)
      {
	 try
	 {
	    setStopWhenSignalTooHigh(
	       Boolean.parseBoolean(settings.getProperty("stopwhensignaltoohigh")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid stopwhensignaltoohigh \"" 
	       + settings.getProperty("stopwhensignaltoohigh") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","stopwhensignaltoohigh");
	 }
      }
      if (settings.getProperty("snrpoormin") != null)
      {
	 try
	 {
	    setSnrPoorMin(
	       Double.parseDouble(settings.getProperty("snrpoormin")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid snrpoormin \"" 
	       + settings.getProperty("snrpoormin") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","snrpoormin");
	 }
      }
      else
      {
	 vMessages.addElement("No snrpoormin supplied");
	 settings.setProperty("usage","snrpoormin");
      }
      if (settings.getProperty("snrnormalmax") != null)
      {
	 try
	 {
	    setSnrNormalMax(
	       Double.parseDouble(settings.getProperty("snrnormalmax")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid snrnormalmax \"" 
	       + settings.getProperty("snrnormalmax") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","snrnormalmax");
	 }
      }
      else
      {
	 vMessages.addElement("No snrnormalmax supplied");
	 settings.setProperty("usage","snrnormalmax");
      }
      if (settings.getProperty("signallevelminimum") != null)
      {
	 try
	 {
	    setSignalLevelMinimum(
	       Integer.parseInt(settings.getProperty("signallevelminimum")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid signallevelminimum \"" 
	       + settings.getProperty("signallevelminimum") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","signallevelminimum");
	 }
      }
      else
      {
	 vMessages.addElement("No Signallevelminimum supplied");
	 settings.setProperty("usage","signallevelminimum");
      }
      if (settings.getProperty("signallevelincrement") != null)
      {
	 try
	 {
	    setSignalLevelIncrement(
	       Integer.parseInt(settings.getProperty("signallevelincrement")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid signallevelincrement \"" 
	       + settings.getProperty("signallevelincrement") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","signallevelincrement");
	 }
      }
      else
      {
	 vMessages.addElement("No signallevelincrement supplied");
	 settings.setProperty("usage","signallevelincrement");
      }
      if (settings.getProperty("signallevelstart") != null)
      {
	 try
	 {
	    setSignalLevelStart(
	       Integer.parseInt(settings.getProperty("signallevelstart")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid signallevelstart \"" 
	       + settings.getProperty("signallevelstart") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","signallevelstart");
	 }
      }
      else
      {
	 vMessages.addElement("No signallevelstart supplied");
	 settings.setProperty("usage","signallevelstart");
      }
      if (settings.getProperty("ignoretrials") != null)
      {
	 try
	 {
	    setIgnoreTrials(
	       Integer.parseInt(settings.getProperty("ignoretrials")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid ignoretrials \"" 
	       + settings.getProperty("ignoretrials") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","ignoretrials");
	 }
      }
      else
      {
	 vMessages.addElement("No ignoretrials supplied");
	 settings.setProperty("usage","ignoretrials");
      }
      if (settings.getProperty("timeoutseconds") != null)
      {
	 try
	 {
	    setTimeoutSeconds(
	       Integer.parseInt(settings.getProperty("timeoutseconds")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid timeoutseconds \"" 
	       + settings.getProperty("timeoutseconds") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","timeoutseconds");
	 }
      }
      if (settings.getProperty("compact") != null)
      {
	 try
	 {
	    setCompact(
	       Boolean.parseBoolean(settings.getProperty("compact")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid compact \"" 
	       + settings.getProperty("compact") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","compact");
	 }
      }
      int i = 0;
      while (settings.getProperty("trials" + i) != null)
      {
	 vTrials.addElement(settings.getProperty("trials" + i));
	 i++;
      } // next trial set
      if (vTrials.size() == 0)
      {
	 vMessages.addElement("No trials supplied");
	 settings.setProperty("usage","trials");
      }
      else
      {
	 getTrialTokens();
      }
      if (settings.getProperty("formid") != null)
      {
	 setFormId(settings.getProperty("formid"));
      }
      if (settings.getProperty("trialsetnumberforminputid") != null)
      {
	 setTrialSetNumberFormInputId(settings.getProperty("trialsetnumberforminputid"));
      }
      if (settings.getProperty("trialsforminputid") != null)
      {
	 setTrialsFormInputId(settings.getProperty("trialsforminputid"));
      }
      if (settings.getProperty("resultforminputid") != null)
      {
	 setResultFormInputId(settings.getProperty("resultforminputid"));
      }
      if (settings.getProperty("meansnrforminputid") != null)
      {
	 setMeanSNRFormInputId(settings.getProperty("meansnrforminputid"));
      }
      if (settings.getProperty("filenameformat") != null)
      {
	 try
	 {
	    setFileNameFormat(
	       new MessageFormat(settings.getProperty("filenameformat")));
	 }
	 catch(Exception exception)
	 {
	    vMessages.addElement(
	       "Invalid filenameformat \"" 
	       + settings.getProperty("filenameformat") + "\": " 
	       + exception.getClass().getSimpleName() 
	       + " - " + exception.getMessage());
	    settings.setProperty("usage","filenameformat");
	 }
      }
      else
      {
	 vMessages.addElement("No filenameformat supplied");
	 settings.setProperty("usage","filenameformat");
      }
      // localization:
      if (settings.getProperty("textStart") != null) textStart = settings.getProperty("textStart");
      if (settings.getProperty("textClear") != null) textClear = settings.getProperty("textClear");
      if (settings.getProperty("textCheckVolume") != null) textCheckVolume = settings.getProperty("textCheckVolume");
      if (settings.getProperty("textClearAllDigitsEntered") != null) textClearAllDigitsEntered = settings.getProperty("textClearAllDigitsEntered");
      if (settings.getProperty("textGoToTheNextStep") != null) textGoToTheNextStep = settings.getProperty("textGoToTheNextStep");
      if (settings.getProperty("textHowFarThroughTheTestYouAre") != null) textHowFarThroughTheTestYouAre = settings.getProperty("textHowFarThroughTheTestYouAre");
      if (settings.getProperty("textProblemLoadingAudio") != null) textProblemLoadingAudio = settings.getProperty("textProblemLoadingAudio");
      if (settings.getProperty("textNext") != null) textNext = settings.getProperty("textNext");
      if (settings.getProperty("textPleaseWait") != null) textPleaseWait = settings.getProperty("textPleaseWait");
      if (settings.getProperty("textFinish") != null) textFinish = settings.getProperty("textFinish");
	 
      return vMessages;
   } // end of loadSettings()

      
   /**
    * Interpretes the {@link #sTrials} list, breaking the string into a list of triplets and storing them in {@link #vTriplets}
    */
   protected void getTrialTokens()
   {
      // randomly pick a set of trials
      Random random = new Random();
      setTrialSetNumber(random.nextInt(vTrials.size()));
      setTrialSet(getTrialSetNumber());

      // load the selected set
      StringTokenizer triplets = new StringTokenizer(
	 getTrials().elementAt(getTrialSet()).toString(),",");
      Vector vOriginalOrder = new Vector();
      iTrial = 0;
      while (triplets.hasMoreTokens())
      {
	 vOriginalOrder.addElement(new Trial(triplets.nextToken().trim()));
      } // next triplet

      // now shuffle them into the triples collection
      vTriplets.clear();
      int iTrialNumber = 0;	 
      while (vOriginalOrder.size() > 0)
      {
	 int iRandomElement = random.nextInt(vOriginalOrder.size());
	 Trial trial = (Trial)vOriginalOrder.elementAt(iRandomElement);
	 trial.setNumber(iTrialNumber++);
	 vTriplets.addElement(trial);
	 vOriginalOrder.removeElementAt(iRandomElement);
      } // next trial

   } // end of getTrialTokens()


   /**
    * Loads the media for the volume check
    */
   public void loadVolumeCheck()
      throws Exception
   {
      URL url = new URL(getMediaUrlBase(), getVolumCheckFile());
      volumeCheck = readFromUrl(url);
   } // end of loadVolumeCheck()


   /**
    * Gets the media for the first and second trials
    */
   public void prepareFirstTrial()
      throws Exception
   {
      sTrialsCsv = "\"Number\",\"Signal\",\"Triplet\",\"Entered\",\"Result\"\r\n";
      device = FactoryRegistry.systemRegistry().createAudioDevice();
      iTrial = -1;
      iPreparedTrial = 0;
      pb.setValue(0);
      // load media for first trial
      Trial firstTrial = (Trial)vTriplets.elementAt(iPreparedTrial);
      firstTrial.setDecibelsSignal(new Integer(iSignalLevelStart));
      String sFile = getFileNameFormat().format(
	 getFileArguments(firstTrial.getCorrectAnswer(), 
			  firstTrial.getDecibelsSignal()));
      URL url = url = new URL(getMediaUrlBase(), sFile);
      byte[] first = readFromUrl(url);
      player = new Player(new ByteArrayInputStream(first), device);
      nextMediaNoisy = null;
      nextMediaQuiet = null;

      // start loading media for second trial
      Trial secondTrial = (Trial)vTriplets.elementAt(iPreparedTrial + 1);
      iSignalLevelNoisy = firstTrial.getDecibelsSignal().intValue()+iSignalLevelIncrement;
      String sFileNoisy = getFileNameFormat().format(
	 getFileArguments(secondTrial.getCorrectAnswer(), new Integer(iSignalLevelNoisy)));
      iSignalLevelQuiet = firstTrial.getDecibelsSignal().intValue()-iSignalLevelIncrement;
      String sFileQuiet = getFileNameFormat().format(
	 getFileArguments(secondTrial.getCorrectAnswer(), new Integer(Math.min(getSignalLevelMaximum(), iSignalLevelQuiet))));
      final URL urlNextNoisy = new URL(getMediaUrlBase(), sFileNoisy);
      final URL urlNextQuiet = new URL(getMediaUrlBase(), sFileQuiet);
      Thread loadMediaThread = new Thread(new Runnable()
	 {
	    public void run()
	    {
	       for (int i = 0; i < 5; i++) // try up to 5 times
	       {
		  try
		  {
		     nextMediaNoisy = readFromUrl(urlNextNoisy);
		     nextMediaQuiet = readFromUrl(urlNextQuiet);
		     break;
		  }
		  catch(Exception exception)
		  {
		     lblMessage.setText(textProblemLoadingAudio + " " + i + ": " + exception);
		     System.err.println(lblMessage.getText());
		  }
	       }
	       iPreparedTrial++;
	    }
	 });
      loadMediaThread.start();
   } // end of prepareFirstTrial()

      
   /**
    * Prepares to start the next trial
    */
   public void prepareNextTrial()
      throws SignalLevelTooHighException, Exception
   {
      btnPlay.setText("Next");
      // wait for media to be ready
      System.out.println("next trial");
      int iLimit = 50;
      int iLoop = 0;
      while (player != null && ++iLoop < iLimit)
      {
	 System.out.println("waiting for player to finish");
	 try { Thread.sleep(100); } catch(Exception exception) {}
      }
      iLoop = 0;
      while (nextMediaNoisy == null && nextMediaQuiet == null
	     && ++iLoop < iLimit)
      {
	 System.out.println("waiting for media");
	 try { Thread.sleep(100); } catch(Exception exception) {}
      }

      System.out.println("creating new player");
      device = FactoryRegistry.systemRegistry().createAudioDevice();

      // decide what media file to play
      Trial lastTrial = (Trial)vTriplets.elementAt(iPreparedTrial - 1);
      Trial thisTrial = (Trial)vTriplets.elementAt(iPreparedTrial);
      if (lastTrial.getResult()) // they got the last answer right
      {
	 // play noisy version
	 player = new Player(new ByteArrayInputStream(
				nextMediaNoisy), device);
	 thisTrial.setDecibelsSignal(new Integer(iSignalLevelNoisy));
      }
      else
      {
	 // play quiet version
	 player = new Player(new ByteArrayInputStream(
				nextMediaQuiet), device);
	 if (iSignalLevelQuiet > iSignalLevelMaximum) 
	 {
	    if (getStopWhenSignalTooHigh())
	    {
	       throw new SignalLevelTooHighException();
	    }
	    else
	    {
	       iSignalLevelQuiet = iSignalLevelMaximum;
	    }
	 }
	 thisTrial.setDecibelsSignal(new Integer(iSignalLevelQuiet));
      }

      nextMediaNoisy = null;
      nextMediaQuiet = null;
      if (iPreparedTrial + 1 < vTriplets.size())
      {
	 Trial trial = (Trial)vTriplets.elementAt(iPreparedTrial + 1);
	 iSignalLevelNoisy = Math.max(
	    thisTrial.getDecibelsSignal().intValue() + iSignalLevelIncrement,
	    iSignalLevelMinimum);
	 String sFile = getFileNameFormat().format(
	    getFileArguments(trial.getCorrectAnswer(), new Integer(iSignalLevelNoisy)));
	 final URL urlNextNoisy = new URL(getMediaUrlBase(), sFile);
	 iSignalLevelQuiet = thisTrial.getDecibelsSignal().intValue()
	    - iSignalLevelIncrement;
	 sFile = getFileNameFormat().format(
	    getFileArguments(
	       trial.getCorrectAnswer(), 
	       new Integer(Math.min(iSignalLevelMaximum, iSignalLevelQuiet))));
	 final URL urlNextQuiet = new URL(getMediaUrlBase(), sFile);
	 Thread loadMediaThread = new Thread(new Runnable()
	    {
	       public void run()
	       {
		  for (int i = 0; i < 5; i++) // try up to 5 times
		  {
		     try
		     {
			nextMediaNoisy = readFromUrl(urlNextNoisy);
			nextMediaQuiet = readFromUrl(urlNextQuiet);
			break;
		     }
		     catch(Exception exception)
		     {
			lblMessage.setText(textProblemLoadingAudio + " " + i + ": "+exception);
			System.err.println(lblMessage.getText());
		     }
		  }
		  iPreparedTrial++;
	       }
	    });
	 loadMediaThread.start();
      }
   } // end of prepareNextTrial()

      
   /**
    * Reads all data given by a URL
    * @param url
    * @return The contents of the URL
    * @throws IOException
    */
   public byte[] readFromUrl(URL url)
      throws IOException
   {
//	 System.out.println("Getting " + url);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      InputStream is = url.openStream();
      byte[] buf = new byte[1024]; 
      int nread;
      int navailable;
      int total = 0;
      while((nread = is.read(buf, 0, buf.length)) >= 0) 
      {
	 os.write(buf, 0, nread);
	 total += nread;
      }
      // System.out.println("Finished " + url);
      return os.toByteArray();
   } // end of readFromUrl()

      
   /**
    * Create a MessageFormat argument array for the given file
    * @param sTriplets
    * @param iSnr
    * @return An array of arguments that could be passed to fmtFileNameFormat
    */
   public Object[] getFileArguments(String sTriplets, Integer iSnr)
   {
      Object[] oArgs = {sTriplets, iSnr};
      return oArgs;
   } // end of getFileArguments()


   /**
    * Action listener method.
    */
   public void actionPerformed(ActionEvent event) 
   {
      String command = event.getActionCommand();
      if (volumeCheckPlayer != null) return;

      if (event.getSource() == btnVolumeCheck)
      {
	 btnPlay.setEnabled(false);
	 lblMessage.requestFocus();
	 new Thread(new Runnable()
	    {
	       public void run()
	       {
		  btnVolumeCheck.setIcon(icoPlaying);
		  try
		  {
		     device = FactoryRegistry.systemRegistry().createAudioDevice();
			   
		     // load media for volume check
		     volumeCheckPlayer = new Player(
			new ByteArrayInputStream(volumeCheck), device);
		     volumeCheckPlayer.play();
		     volumeCheckPlayer.close();
		  }
		  catch (Throwable ex)
		  {
		     lblMessage.setText(textProblemLoadingAudio + ": "+ex);
		     System.err.println(lblMessage.getText());
		  }
		  finally
		  {
		     volumeCheckPlayer = null;
		     btnVolumeCheck.setIcon(null);
		     btnPlay.setEnabled(true);
		  }
	       }
	    }).start();
      }
      else if (event.getSource() == btnPlay)
      {
	 if (btnPlay.getText().length() > 0)
	 {
	    if (btnVolumeCheck != null)
	    {
	       System.out.println("Starting...");
	       lblMessage.setText(textPleaseWait);
	       remove(btnVolumeCheck);
	       btnVolumeCheck = null;
	       add(lblMessage, BorderLayout.NORTH);
	       
	       btn0.setEnabled(true);
	       btn1.setEnabled(true);
	       btn2.setEnabled(true);
	       btn3.setEnabled(true);
	       btn4.setEnabled(true);
	       btn5.setEnabled(true);
	       btn6.setEnabled(true);
	       btn7.setEnabled(true);
	       btn8.setEnabled(true);
	       btn9.setEnabled(true);

	       layout();
	       try
	       {
		  prepareFirstTrial();
	       }
	       catch (Throwable ex)
	       {
		  lblMessage.setText("Problem preparing first trial: "+ex);
		  System.err.println(lblMessage.getText());
	       }
	    }
	    else
	    {
	       Trial trial = (Trial) vTriplets.elementAt(iTrial);
	       trial.setAnswer(lblMessage.getText().replaceAll(" ", ""));
	       System.out.println("Result: " + trial);
	       sTrialsCsv += ""+trial.getNumber()
		  + "," + trial.getDecibelsSignal()
		  + ",\"" + trial.getCorrectAnswer() + "\""
		  + ",\"" + trial.getAnswer() + "\""
		  + "," + trial.getResult()
		  + "\r\n";	       
	    }
	    btnPlay.setText("");
	    btnClear.setEnabled(false);
	    lblMessage.requestFocus();
	 
	    if (iTrial == vTriplets.size() - 1)
	    {
	       finishTest();
	    }
	    else
	    {
	       try
	       {
		  iTrial++;
		  pb.setValue(iTrial);
		  
		  lblMessage.setText("");
		  playThread = new Thread(new Runnable()
		     {
			public void run()
			{
			   try
			   {
			      try
			      {
				 if (iTrial > 0)
				 {
				    try
				    {
				       prepareNextTrial();
				    }
				    catch (SignalLevelTooHighException sth)
				    {
				       throw sth;
				    }
				    catch(Throwable t)
				    {
				       lblMessage.setText(
					  "Could not prepare next trial: " 
					  + t.getClass().getSimpleName() 
					  + " - " + t.getMessage());
				       System.err.println(lblMessage.getText());
				    }
				 }
				 btnPlay.setIcon(icoPlaying);
				 btnPlay.setText("");
				 player.play();
			      }
			      catch (SignalLevelTooHighException sth)
			      {
				 finishTest();
				 return;
			      }
			      catch (Throwable ex)
			      {
				 lblMessage.setText("Problem playing audio: "+ex);
				 System.err.println(lblMessage.getText());
			      }		
			      finally
			      {
				 player.close();
				 player = null;
			      }
System.out.println("player finished - " + lblMessage.getText().length() + " " + iTrial + " " + iPreparedTrial + " " + vTriplets.size());
			      if ((iTrial == iPreparedTrial - 1
				   || iTrial == vTriplets.size() - 1)
				  && lblMessage.getText().length() >= 5)
			      {
				 if (iTrial == vTriplets.size() - 1)
				 {
				    btnPlay.setText(textFinish);
				    btnPlay.setIcon(null);
				 }
				 else
				 {
				    btnPlay.setText(textNext); // btnPlay.setEnabled(true); 
				    btnPlay.setIcon(icoPlay);
				 }
			      }
			      else
			      {
				 btnPlay.setIcon(null);
				 try 
				 { 
				    Thread.sleep(getTimeoutSeconds() * 1000); 
				    btnPlay.setText(textNext); // btnPlay.setEnabled(true); 
				    btnPlay.setIcon(icoPlay);
				 }
				 catch(InterruptedException exception) {}
			      }
			   }
			   finally
			   {
			      playThread = null;
			   }
			}
		     });
		  playThread.start();
	       }
	       catch(Throwable t)
	       {
		  lblMessage.setText(
		     "Could not play media: " 
		     + t.getClass().getSimpleName() 
		     + " - " + t.getMessage());
		  System.err.println(lblMessage.getText());
	       }
	    } // there are more trials
	 } // button is enabled for playing
      }
      else if (event.getSource() == btnClear)
      {
	 lblMessage.setText("");
	 btnPlay.setText("");
	 btnPlay.setIcon(null);
	 btnClear.setEnabled(false);
	 lblMessage.requestFocus();
      }
      else // digit
      {
	 if (lblMessage.getText().length() < 5)
	 {
	    lblMessage.setText((lblMessage.getText() + " " + command).trim());
	 }
	 btnClear.setEnabled(true);
	 if (lblMessage.getText().length() >= 5)
	 {
	    if (iTrial < vTriplets.size())
	    {
	       if ((iTrial == iPreparedTrial - 1 
		    || iTrial == vTriplets.size() - 1)
		   && player == null)
	       {
		  if (iTrial == vTriplets.size() - 1)
		  {
		     btnPlay.setText(textFinish);
		  }
		  else
		  {
		     btnPlay.setText(textNext); 
		     btnPlay.setIcon(icoPlay); 
		  }
		  if (playThread != null) playThread.interrupt();
	       }
	    }
	 }
      }
   } // end of actionPerformed()

   
   /**
    * Finish the test by posting the results.
    */
   public void finishTest()
   {
      lblMessage.setText(textPleaseWait);
      pb.setValue(pb.getMaximum());
      
      btn0.setEnabled(false);
      btn1.setEnabled(false);
      btn2.setEnabled(false);
      btn3.setEnabled(false);
      btn4.setEnabled(false);
      btn5.setEnabled(false);
      btn6.setEnabled(false);
      btn7.setEnabled(false);
      btn8.setEnabled(false);
      btn9.setEnabled(false);
      btnPlay.setEnabled(false);
      btnClear.setEnabled(false);
	 
      // compute overall result

      // ignore first n trials
      Enumeration enTriplets = vTriplets.elements();
      for (int i = 0; i < iIgnoreTrials && enTriplets.hasMoreElements(); i++) enTriplets.nextElement(); 

      // take average snr for remaining trials
      double dTotal = 0;
      int iCount = 0;
      double dMean = Double.MAX_VALUE;
      try
      {
	 
	 while (enTriplets.hasMoreElements())
	 {
	    Trial trial = (Trial) enTriplets.nextElement();
	    iCount++;
	    dTotal += (double)trial.getDecibelsSignal().intValue();
	 }
	 if (iCount > 0)
	 {
	    dMean = dTotal / iCount;
//	 lblMessage.setText("Mean SNR: " + dTotal + "/" + iCount + "=" + dMean);
	    
	    if (dMean > dSnrPoorMin) iResultCode = RESULT_POOR;
	    else if (dMean < dSnrNormalMax) iResultCode = RESULT_NORMAL;
	    else iResultCode = RESULT_INCONCLUSIVE;
	 }
	 
      }
      catch(NullPointerException exception)
      {
	 iResultCode = RESULT_INCOMPLETE;
      }	 
      // Post results to URL
      try
      {
	 setFormInputValue(getTrialSetNumberFormInputId(), ""+getTrialSet());
	 setFormInputValue(getTrialsFormInputId(), getTrialsCsv());
	 setFormInputValue(getResultFormInputId(), ""+getResultCode());
	 setFormInputValue(getMeanSNRFormInputId(), ""+dMean);
	 if (getFormId() != null) submitForm(getFormId());
      }
      catch (Throwable t)
      {
//	 lblMessage.setText(t.getMessage());
	 System.out.println("Could not post results: "+ t);
      }	 
   } // end of finishTest()

   
   /**
    * Sets the value of the given input element in the hosting HTML document.
    * @param sElementId
    * @param sValue
    * @throws Exception
    */
   public void setFormInputValue(String sElementId, String sValue)
      throws Exception
   {
      if (sElementId == null || sValue == null) return;	
      JSObject.getWindow(this).eval(
	 "document.getElementById(\""+sElementId+"\").value = \""
	 +sValue.replaceAll("\"","\\\\\"").replaceAll("\r","\\\\r").replaceAll("\n","\\\\n")
	 +"\";"); 
   } // end of setFormInputValue()

   /**
    * Submits the form with the given ID in the hosting HTML document.
    * @param sId
    * @throws Exception
    */
   public void submitForm(String sId)
      throws Exception
   {
      if (sId == null) return;	
      JSObject.getWindow(this).eval(
	 "document.getElementById(\""+sId+"\").submit();"); 
   } // end of setFormInputValue()


   // KeyListener methods
   public void keyPressed(KeyEvent e)
   {
      switch (e.getKeyCode())
      {
	 case KeyEvent.VK_BACK_SPACE:
	    if (lblMessage.getText().length() > 0)
	    {
	       lblMessage.setText(
		  lblMessage.getText().substring(
		     0, lblMessage.getText().length() - 1).trim());
	       if (btnClear.isEnabled())
	       {
		  btnPlay.setText("");
		  btnPlay.setIcon(null);
	       }
	       if (lblMessage.getText().length() == 0)
	       {
		  btnClear.setEnabled(false);
	       }
	       lblMessage.requestFocus();
	    }
	    break;
	 case KeyEvent.VK_DELETE: btnClear.doClick(); break;
	 case KeyEvent.VK_ENTER: btnPlay.doClick(); break;
	 case KeyEvent.VK_0: case KeyEvent.VK_NUMPAD0: btn0.doClick(); break;
	 case KeyEvent.VK_1: case KeyEvent.VK_NUMPAD1: btn1.doClick(); break;
	 case KeyEvent.VK_2: case KeyEvent.VK_NUMPAD2: btn2.doClick(); break;
	 case KeyEvent.VK_3: case KeyEvent.VK_NUMPAD3: btn3.doClick(); break;
	 case KeyEvent.VK_4: case KeyEvent.VK_NUMPAD4: btn4.doClick(); break;
	 case KeyEvent.VK_5: case KeyEvent.VK_NUMPAD5: btn5.doClick(); break;
	 case KeyEvent.VK_6: case KeyEvent.VK_NUMPAD6: btn6.doClick(); break;
	 case KeyEvent.VK_7: case KeyEvent.VK_NUMPAD7: btn7.doClick(); break;
	 case KeyEvent.VK_8: case KeyEvent.VK_NUMPAD8: btn8.doClick(); break;
	 case KeyEvent.VK_9: case KeyEvent.VK_NUMPAD9: btn9.doClick(); break;
      }
   }
   public void keyReleased(KeyEvent e)
   {}
   public void keyTyped(KeyEvent e)
   {}

   // WindowListener methods:

   /**
    * Called when the user tries to close the window.
    */
   public void windowClosing(WindowEvent e) 
   {
      exit();
   } // end of windowClosing()

   /**
    * Called when the window is brought to the front.
    */
   public void windowActivated(WindowEvent e) 
   {} // end of windowActivated()
  
      /**
       * Called when the window closes.
       */
   public void windowClosed(WindowEvent e)
   {}  // end of windowClosed()
   
   /**
    * Called when the window is no longer at the front.
    */
   public void windowDeactivated(WindowEvent e)
   {} // end of windowDeactivated()
  
      /**
       * Called when the window is brought up from an icon.
       */
   public void windowDeiconified(WindowEvent e)
   {}  // end of windowDeiconified()
  
   /**
    * Called when the window is iconified.
    */
   public void windowIconified(WindowEvent e)
   {} // end of windowIconified()
  
      /**
       * Called when the winodw is first opened.
       */
   public void windowOpened(WindowEvent e) 
   {} // end of windowOpened()

      /**
       * Exists the java applet.
       */
   public boolean exit()
   {
      System.exit(0);
      return true;
   } // end of exit()
} // end of class DigitTripletsTest
