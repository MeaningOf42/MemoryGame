import edu.cmu.ri.createlab.terk.robot.finch.Finch;
import java.awt.Color;
import java.util.*;
import java.io.*;

public class game {
	public static void main(String[] arguments) {
		
		// Creates a random objec for use later in the program.
		Random randomGen = new Random();
		
		Scanner input = new Scanner(System.in);
		// To do add GUI to make look nicer
		System.out.print("Enter starting difficulty: ");
		int difficulty = input.nextInt();
		// To do: use http://usb4java.org/quickstart/javax-usb.html to work out how many finch robots there are to connect
		// Instead we just ask user.
		System.out.print("Enter number of finches connected: ");
		int numFinches = input.nextInt();
		input.close();
		
		// connects to each finch, and stores each connection in an array.
		Finch[] finches = new Finch[numFinches];
		for (int i = 0; i< numFinches; i++) {
			finches[i] = new Finch();
		}
		
		// Creates an array of colors equally spaced around the edge of the color wheel.
		Color[] colorPalette = new Color[5];
		for (int i = 0; i < colorPalette.length; i++) {
			colorPalette[i] = Color.getHSBColor(i*1.f/colorPalette.length, 1.f, 1.f);
		}
		
		int score = 0;
		boolean wonRound = true;
		
		// Gives instructions and pauses before starting
		while (wonRound) {
			// Creates an array of colors of length difficulty, which the player will be shown and have to remember.
			// All colors are taken randomly from the color palette.
			Color[] colorsToRemember = new Color[difficulty];
			for (int i = 0; i < colorsToRemember.length; i++)
				colorsToRemember[i] = colorPalette[randomGen.nextInt(colorPalette.length)];
			
			System.out.println("In 3 seconds a sequence of "+Integer.toString(difficulty)+" colors will be displayed on the finches, remeber the order that colors are displayed: ");
			finches[0].sleep(3000);
		
			// Displays the colors on one finch at a time.
			for (int i = 0; i < colorsToRemember.length; i++) {
				finches[i%numFinches].setLED(colorsToRemember[i]);
				finches[i%numFinches].sleep(500);
				finches[i%numFinches].setLED(0,0,0);
				finches[i%numFinches].sleep(500);
			}
			System.out.println("Sequence over. Hold on to that sequence for " + Integer.toString(Math.min(5, difficulty+1))+" seconds.");
			// Sleep for specified time to make it harder to remember
			finches[0].sleep(Math.min(5000, difficulty*1000+1000));
		
			System.out.println("Now tap on the colors in the correct order. ");
		
		
			for (Color color : colorsToRemember) {
				int correctFinch = randomGen.nextInt(finches.length);
				for (int i = 0; i < finches.length; i++) {
					if (i == correctFinch) {
						finches[i].setLED(color);
					}
					else {
						Color falseColor;
						while ((falseColor=colorPalette[randomGen.nextInt(colorPalette.length)]) == color) {}
						finches[i].setLED(falseColor);
						// Uncoment next line to make game easy for devolopers.
						//finches[i].setLED(0,0,0);
					}
				}
			
				int tapped = -1;
				while (tapped == -1) {
					for (int i = 0; i < finches.length; i++) {
						if (finches[i].isObstacle()) {
							tapped = i;
						}
						if (finches[i].isTapped())
							wonRound = false;
					}
				}
			
				if (tapped != correctFinch) {
					wonRound = false;
					break;
				}
				
				score += difficulty;
				System.out.println("Correct you got " + Integer.toString(difficulty) + " points! score = " + Integer.toString(score));
				// turn all the LEDs Green to show input was correct
				setAllLights(finches, Color.GREEN);
				finches[0].sleep(1000);
			}
		
			if (wonRound) {
				System.out.println("You remembered the Sequence! Level Up!");
				difficulty++;
				System.out.println("Dificulty inreased to: " + Integer.toString(difficulty));
				// flash lights for celebration
				for (int i = 0; i<10; i++) {
					setAllLights(finches, Color.GREEN);
					finches[0].sleep(100);
					setAllLights(finches, Color.BLACK);
					finches[0].sleep(100);
				}
			}
		}
		
		System.out.println("Game Over! Your final score was: "+Integer.toString(score));
		System.out.println();
		
		ArrayList<List<Object>> scores = readScores("H:\\eclipseWorkspace\\MemoryGame\\src\\scores.txt");
		System.out.println("High Scores: ");
		System.out.println();
		printTable(Arrays.asList("Rank", "Name", "Score"), scores, 15);
		System.out.println();
		// turn LEDs Red
		/*setAllLights(finches, 255,0,0);
		// Pause on current color for effect
		finches[0].sleep(3000);
		
		

		for (Finch finch: finches) {
			finch.quit();
		}*/
		System.out.println("Done");
	}
	
	static void setAllLights(Finch[] finches, Color color) {
		for (Finch finch : finches) {
			finch.setLED(color);
		}
	}
	
	static ArrayList<List<Object>> readScores(String fileName) {
		ArrayList<List<Object>> scores = new ArrayList<List<Object>>();
		
        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] lineParts = line.split(",");
                List<Object> toAdd = Arrays.asList(lineParts[0], Integer.parseInt(lineParts[1]));
                scores.add(toAdd);
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Scores file not found.");                
        }
        catch(IOException ex) {
            System.out.println("Error reading scores file.");
        }
		return scores;
	}
	
	static void printTable(List<Object> headings, ArrayList<List<Object>> data, int spaces) {
		// Print out field headings
		System.out.println("    " + new String(new char[spaces*headings.size()]).replace('\0', '-'));
		int i = 0;
		String field;
		System.out.print("    | ");
		for (; i<headings.size()-1; i++) {
			field = String.valueOf(headings.get(i));
			System.out.print(field + new String(new char[spaces - field.length()-2]).replace('\0', ' ')+"| ");
		}
		field = String.valueOf(headings.get(i));
		System.out.println(field+new String(new char[spaces - field.length()-2])+"|");
		System.out.println("    " + new String(new char[spaces*headings.size()]).replace('\0', '-'));
		
		for (i =0; i<data.size(); i++) {
			String rankStr = String.valueOf(i+1);
			System.out.print("    | ");
			System.out.print(rankStr + new String(new char[spaces- rankStr.length()-2]).replace('\0', ' ')+"| ");
			List<Object> row = data.get(i);
			field = String.valueOf(headings.get(i));
			int j = 0;
			for (; j<row.size()-1; j++) {
				field = String.valueOf(row.get(j));
				System.out.print(field + new String(new char[spaces - field.length()-2]).replace('\0', ' ')+"| ");
			}
			field = String.valueOf(row.get(j));
			System.out.println(field+new String(new char[spaces - field.length()-2])+"|");
			//System.out.println(new String(new char[spaces*headings.size()]).replace('\0', '-'));
		}
		System.out.println("    " + new String(new char[spaces*headings.size()]).replace('\0', '-'));
	}	
}
