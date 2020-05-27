//
// (c) 2010, Robert Fromont - robert@fromont.net.nz
//
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

/**
 * A single trial during the Digit Triplets Test
 * @author Robert Fromont robert@fromont.net.nz
 */

public class Trial
{
   // Attributes:
   
   /**
    * Signal level in decibels
    * @see #getDecibelsSignal()
    * @see #setDecibelsSignal(Integer)
    */
   protected Integer iDecibelsSignal;
   /**
    * Getter for {@link #iDecibelsSignal}: Signal level in decibels
    * @return Signal level in decibels
    */
   public Integer getDecibelsSignal() { return iDecibelsSignal; }
   /**
    * Setter for {@link #iDecibelsSignal}: Signal level in decibels
    * @param iNewDecibelsSignal Signal level in decibels
    */
   public void setDecibelsSignal(Integer iNewDecibelsSignal) { iDecibelsSignal = iNewDecibelsSignal; }


   /**
    * Trial number
    * @see #getNumber()
    * @see #setNumber(int)
    */
   protected int iNumber;
   /**
    * Getter for {@link #iNumber}: Trial number
    * @return Trial number
    */
   public int getNumber() { return iNumber; }
   /**
    * Setter for {@link #iNumber}: Trial number
    * @param iNewNumber Trial number
    */
   public void setNumber(int iNewNumber) { iNumber = iNewNumber; }

   /**
    * The correct answer for the trial.
    * @see #getCorrectAnswer()
    * @see #setCorrectAnswer(String)
    */
   protected String sCorrectAnswer;
   /**
    * Getter for {@link #sCorrectAnswer}: The correct answer for the trial.
    * @return The correct answer for the trial.
    */
   public String getCorrectAnswer() { return sCorrectAnswer; }
   /**
    * Setter for {@link #sCorrectAnswer}: The correct answer for the trial.
    * @param sNewCorrectAnswer The correct answer for the trial.
    */
   public void setCorrectAnswer(String sNewCorrectAnswer) { sCorrectAnswer = sNewCorrectAnswer; }


   /**
    * The answer given by the subject.
    * @see #getAnswer()
    * @see #setAnswer(String)
    */
   protected String sAnswer;
   /**
    * Getter for {@link #sAnswer}: The answer given by the subject.
    * @return The answer given by the subject.
    */
   public String getAnswer() { return sAnswer; }
   /**
    * Setter for {@link #sAnswer}: The answer given by the subject.
    * @param sNewAnswer The answer given by the subject.
    */
   public void setAnswer(String sNewAnswer) { sAnswer = sNewAnswer; }
   
   // Methods:
   
   /**
    * Default constructor
    */
   public Trial()
   {
   } // end of constructor

   /**
    * Constructor
    * @param correctAnswer The correct answer for the trial.
    */
   public Trial(String correctAnswer)
   {
      setCorrectAnswer(correctAnswer);
   } // end of constructor

   /**
    * Whether or not the answer was correct
    * @return true if the subject answered correctly, false otherwise
    */
   public boolean getResult()
   {
      return sCorrectAnswer.equals(sAnswer);
   } // end of getResult()

   
   /**
    * A human-readible representation of the trial.
    * @return A human-readible representation of the trial.
    */
   public String toString()
   {
      if (sAnswer == null)
      {
	 return "" + iNumber + " \""+sCorrectAnswer+"\" signal: " + iDecibelsSignal;
      }
      else
      {
	 return "" + iNumber + " \""+sCorrectAnswer+"\" signal: " + iDecibelsSignal + " answer: \"" + sAnswer + "\" " + (getResult()?"correct":"incorrect");
      }
   } // end of toString()


} // end of class Trial
