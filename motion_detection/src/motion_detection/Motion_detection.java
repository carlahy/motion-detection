package motion_detection;

//Created by Carla Hyenne 10/11/15
//Resources
//http://www.pyimagesearch.com/2015/05/25/basic-motion-detection-and-tracking-with-python-and-opencv/
//https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
//http://docs.opencv.org/2.4/modules/imgproc/doc/imgproc.html

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.VideoCapture;

public class Motion_detection {
	
	static Mat frameRender = null;
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Initialise jframe
		JFrame jframe = new JFrame("Motion Detection");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel videopanel = new JLabel();
		jframe.setContentPane(videopanel);
		jframe.setSize(640,480);
		jframe.setVisible(true);
		
		//Open camera
		VideoCapture camera = new VideoCapture(0);
		
		//Initialise variables
		Mat frame = new Mat();
		Mat gray = new Mat();
		Mat frameDelta = new Mat(frame.size(), CvType.CV_8UC1);
		Mat frameTrans = new Mat(frame.size(), CvType.CV_8UC1);
		
		int i = 0;
		Size sz = new Size(640, 480);	
		
		while(true) {
			if(camera.read(frame)) {
				Imgproc.resize(frame, frame, sz);
				frameRender = frame.clone();
				//Convert to gray scale and blur
				gray = new Mat(frame.size(), CvType.CV_8UC1);
				Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.GaussianBlur(gray, gray, new Size(3,3), 0);
				
				//If first frame, ie uninitialised 
				if (i == 0) {	
                    frameDelta = gray.clone();
				}
				else {	
					//Calculate absolute difference between frames and set threshold
					Core.absdiff(frameTrans, gray, frameDelta);
					Imgproc.adaptiveThreshold(frameDelta, frameDelta, 255, 
							Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 2);
					Imgproc.dilate(frameDelta, frameDelta, new Mat());
					
					//Draw contours and bounding rectangle
					detect_contours(frameDelta, frameRender);
				}
				
				i=1;
	
				ImageIcon image = new ImageIcon(mat2bufferedImage(frameRender));
	            videopanel.setIcon(image);
	            videopanel.repaint();
				frameTrans = gray.clone(); //Update transition frame
			}
		}
	}
	
	public static BufferedImage mat2bufferedImage(Mat in) {
        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(".jpg", in, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedImage out = null;
        try {
            out = ImageIO.read(inputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return out;
    }

	public static void detect_contours (Mat ofFrame, Mat toFrame) {

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(ofFrame.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		double min_area = 10;
		
		for(MatOfPoint contour : contours) { 
			if (Imgproc.contourArea(contour) > min_area) {
				//Compute bounding rectangle
				//Rect rect = Imgproc.boundingRect(contour);
				//Imgproc.rectangle(toFrame, rect.br(), rect.tl(), new Scalar(0, 255, 0));
			
				//Draw contour outline
				Imgproc.drawContours(frameRender, contours, contours.indexOf(contour), new Scalar(0,0,255));
			}
		}
	}
}
