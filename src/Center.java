
import java.util.Scanner;

import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.enums.PoseType;

public class Center {

	public Center() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
			Hub hub = new Hub();
			System.out.println("input target MAC");
			Scanner s = new Scanner(System.in);
			String mac = s.nextLine();
			System.out.println("Attempting to find a Myo...");
			Myo myo = hub.waitForMyo(10000);
			
			if (myo == null) {
				throw new RuntimeException("Unable to find a Myo!");
			}
			
			System.out.println("Connected to a Myo armband!");
			DataCollector dataCollector = new DataCollector();
			hub.addListener(dataCollector);

			while (true) {
				hub.run(1000 / 20);
				if(dataCollector.getPose().getType()==PoseType.DOUBLE_TAP) {
					LSHack.message("I don't like you",mac);
					System.out.println("double tap");
				}else if(dataCollector.getPose().getType()==PoseType.FINGERS_SPREAD) {
					LSHack.blank(-1, mac);
					System.out.println("spread");
				}else if(dataCollector.getPose().getType()==PoseType.FIST) {
					LSHack.shutdown(mac);
					System.out.println("fist");
				}else if(dataCollector.getPose().getType()==PoseType.WAVE_IN) {
					//You can add an action if you want
					System.out.println("Wave in");
				}else if(dataCollector.getPose().getType()==PoseType.WAVE_OUT) {
					//add an action if you want
					System.out.println("wave out");
				}else if(dataCollector.getPose().getType()==PoseType.REST||dataCollector.getPose().getType()==PoseType.UNKNOWN) {
					//do nothing I think
				}
			}
		} catch (Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}

	}

}
