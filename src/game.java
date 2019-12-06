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
		
		
		// connects to each finch, and stores each connection in an array.
		Finch[] finches = new Finch[numFinches];
		for (int i = 0; i< numFinches; i++) {
			finches[i] = new Finch();
		}
		
		updateMusic(finches, false);
		
		// Creates an array of colors equally spaced around the edge of the color wheel.
		Color[] colorPalette = new Color[numFinches];
		for (int i = 0; i < colorPalette.length; i++) {
			colorPalette[i] = Color.getHSBColor(i*1.f/colorPalette.length, 1.f, 1.f);
		}
		
		updateMusic(finches, false);
		
		int score = 0;
		boolean wonRound = true;
		
		ArrayList<Integer> colorsToRemember = new ArrayList<Integer>();
		for (int i = 0; i < difficulty; i++)
			colorsToRemember.add(randomGen.nextInt(numFinches));
		
		// Gives instructions and pauses before starting
		while (wonRound) {
			// Creates an array of colors of length difficulty, which the player will be shown and have to remember.
			// All colors are taken randomly from the color palette.
			
			
			System.out.println("In 3 seconds a sequence of "+Integer.toString(difficulty)+" colors will be displayed on the finches, remeber the order that colors are displayed: ");
			musicalWait(3000, finches, false);
		
			// Displays the colors on one finch at a time.
			for (int i = 0; i < colorsToRemember.size(); i++) {
				setColorIndex(true, colorsToRemember.get(i), colorPalette, finches);
				musicalWait(500, finches, false);
				setColorIndex(false, colorsToRemember.get(i), colorPalette, finches);
				musicalWait(500, finches, false);
			}
			System.out.println("Sequence over. Hold on to that sequence for " + Integer.toString(Math.min(5, difficulty+1))+" seconds.");
			// Sleep for specified time to make it harder to remember
			musicalWait(Math.min(5000, difficulty*1000+1000), finches, false);
			System.out.println("Now tap on the colors in the correct order. ");
		
			// Loops over every color the user had to remember.
			for (int colorI : colorsToRemember) {
				showOptionLights(colorPalette, finches);
				
				// This section waits (musically) until the user has covered a robot, then checks if they tapped the correct one.
				int tapped = -1;
				while (tapped == -1) {
					for (int i = 0; i < finches.length; i++) {
						if (finches[i].isObstacle()) {
							tapped = i;
						}
					}
					updateMusic(finches, false);
				}
			
				if (tapped != colorI) {
					wonRound = false;
					break;
				}
				
				// If the user was correct their score increases, all lights go green and the loop continues.
				score += difficulty;
				System.out.println("Correct you got " + Integer.toString(difficulty) + " points! score = " + Integer.toString(score));
				setAllLights(finches, Color.GREEN);
				musicalWait(1000, finches, false);
			}
			
			// If the user makes it to the end of sequence, flash the lights and 
			if (wonRound) {
				colorsToRemember.add(randomGen.nextInt(colorPalette.length));
				System.out.println("You remembered the Sequence! Level Up!");
				difficulty++;
				System.out.println("Dificulty inreased to: " + Integer.toString(difficulty));
				// flash lights for celebration
				for (int i = 0; i<10; i++) {
					setAllLights(finches, Color.GREEN);
					musicalWait(100, finches, false);
					setAllLights(finches, Color.BLACK);
					musicalWait(100, finches, false);
				}
			}
		}
		
		// When they die inform them as such, then play the death song.
		System.out.println("Game Over! Your final score was: "+Integer.toString(score));
		System.out.println();
		musicalWait(3000, finches, true);
		
		// Read highscores from text file 
		ArrayList<List<Object>> scores = readScores("H:\\eclipseWorkspace\\MemoryGame\\src\\scores.txt");
		System.out.println("High Scores: ");
		System.out.println();
		printTable(Arrays.asList("Rank", "Name", "Score"), scores, 15);
		System.out.println();
		
		// Scanning input is bad, after a nextint, one nextline is used instantly, so just have one that isn't used for anything.
		input.nextLine();
		
		// If user wishes add their score to the leaderboard.
		System.out.print("Do you want to add your name to the highscore chart? (y for yes, anything else is no): ");
		if (input.nextLine().equals("y")) {
			try {
				System.out.print("Enter your name: ");
				addToTable(input.nextLine(), score, scores, "H:\\eclipseWorkspace\\MemoryGame\\src\\scores.txt");
			}
			// This catch block happens if user tries putting comma in name to create fake score.
			catch (IllegalArgumentException e) {
				System.out.println("G3t 0wn3d N00B H4ck3r");
			}
			
			catch (Exception e) {
				System.out.println("Writting your highscores did't work, sorry.");
			}
		}
		
		for (Finch finch: finches) {
			finch.quit();
		}
		System.out.println("Done");
		input.close();
	}
	
	// As each finch only displays 1 color, I made a fuction to turn a color / robot on by only specifying 1 value.
	static void setColorIndex(boolean state, int colorI, Color[] colorPalette, Finch[] finches) {
		finches[colorI].setLED(state ? colorPalette[colorI] : Color.BLACK);
	}
	
	// This just turns all the robots on to their specified color.
	static void showOptionLights(Color[] colorPalette, Finch[] finches) {
		for (int i = 0; i < finches.length; i++) {
			setColorIndex(true, i, colorPalette, finches);
		}
	}
	
	// This turns all lights to the same color, useful for flashing all lights as feedback.
	static void setAllLights(Finch[] finches, Color color) {
		for (Finch finch : finches) {
			finch.setLED(color);
		}
	}
	
	// Parses a text file called scores.txt and returns a list of lists containing the information of the leaderboard.
	// The leaderboard is set up so it always stores things in order.
	
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
	
	// Use a list of lists to print out the table of highscores with nice formatting.
	
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
	
	// Takes the players score to be added, and adds it to the correct spot in the leaderboard.
	static void addToTable(String name, int score, ArrayList<List<Object>> prevScores, String fileName) 
			throws IOException {
		// Assert doesn't work on library computers hence this pattern to avoid people making there own highscores.
		if (!(name.indexOf(',') == -1)) {
			throw new IllegalArgumentException("Ha, you can't inject highscores that easy");
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		
		boolean writtenNew = false;
		for (List<Object> Line : prevScores) {
			if (((int) Line.get(1) < score) && !writtenNew) {
				writer.write(name + "," + String.valueOf(score)+"\n");
				writtenNew = true;
			}
			writer.write(Line.get(0) + "," + String.valueOf(Line.get(1))+"\n");
		}
		if (!writtenNew) {
			writer.write(name + "," + String.valueOf(score)+"\n");
		}
		writer.close();
	}
	
	// Waits for a specific time whilst also updating the god awful music. Why did I think anything on these buzzer would ever sound good?
	static void musicalWait(int time, Finch[] finches, boolean dead) {
		long startT = System.currentTimeMillis();
		while (System.currentTimeMillis() - startT < time) {
			// It actually sounds really weird if you don't wait at leatst 20 miliseconds. IDK why though.
			// To be looked into in future.
			finches[0].sleep(20);
			updateMusic(finches, dead);
		}
	}
	
	// Updates each robot so that it is playing the correct not for the present.
	static void updateMusic (Finch[] finches, boolean dead) {
		// Musical note's, as there are 4 finches 1 note can be played on each.
		
		String root  = "ccccccccddddddddeeeeeeeeccccccccaaaaaaaaffffffffgggggggg";
		String third = "eeeeeeeeffffffffggggggggeeeeeeeeccccccccaaaaaaaabbbbbbbb";
		String fith  = "g-g-g-g-a-a-a-a-b-b-b-b-g-g-g-g-e-e-e-e-c-c-c-c-d-d-d-d-";
		String death = "cegbcegbdfacdfacegbdegbdcegbcegbacegacegfacefacegbdfgbdf";
		
		// Conversion of letter notes to frequencies, I multiplied everything by two to increase an octave.
		
		Dictionary noteConversions = new Hashtable();
		noteConversions.put('c', 261*2);
		noteConversions.put('d', 293*2);
		noteConversions.put('e', 329*2);
		noteConversions.put('f', 349*2);
		noteConversions.put('g', 392*2);
		noteConversions.put('a', 440*2);
		noteConversions.put('b', 493*2);
		
		// There is no way the robots can play 3 hertz so this is effectively silence.
		// Couldn't find a way to stop the robot from playing a note, so this was the best I could come up with.
		noteConversions.put('-', 3);
		
		// Work out where in the composition you are based on the current time
		int noteIndex = (int)(System.currentTimeMillis() / 290) % root.length();
		
		// play a different thing when you die for interactivity.
		if (!dead) {
			
			// Switch statement keeps priority of most important notes should there not be a full choir of finches.
			switch (Math.min(4, finches.length)) {
			case 4 : finches[3].buzz((int) noteConversions.get(third.charAt(noteIndex)),  10000);
			case 3 : finches[2].buzz((int) noteConversions.get(root.charAt(noteIndex)),  10000);
			case 2 : finches[2].buzz((int) noteConversions.get(fith.charAt(noteIndex)),  10000);
			}
			finches[2].buzz((int) noteConversions.get(death.charAt(noteIndex)),  10000);
			
		}
		else {
			switch (Math.min(3, finches.length)) {
			case 4 : finches[3].buzz((int) noteConversions.get(root.charAt(noteIndex)),  10000);
			case 3 : finches[2].buzz((int) noteConversions.get(third.charAt(noteIndex)),  10000);
			}
			finches[2].buzz((int) noteConversions.get(fith.charAt(noteIndex)),  10000);
		}
	}
}
