package com.company;

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import javax.sound.midi.*;

public class Main {

     JFrame frame;
     ArrayList<JCheckBox> checkboxArrayList;
     Sequencer player;
     Sequence seq;
     Track track;
     JPanel mainpanel;

     String[] instrument_names={"Bass drum","Closed Hit-Hat","Open Hit-Hat","Acoustic Snare","Crash Cymbal","Hand Clap","High Tom"," Hi Bingo","Maracas","Whistle","Low Conga","Cowbell","Vibraslap","Low-mid Tom","High Agogo","Open Hi Conga"};
     int[] instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    public static void main(String[] args) {
	    new Main().built_gui();
    }

    void built_gui()
    {
        frame=new JFrame("Music Player");
        checkboxArrayList=new ArrayList<>();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout=new BorderLayout();
        JPanel background= new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        Box buttonbox=new Box(BoxLayout.Y_AXIS);
        JButton startbtn=new JButton("start");
        startbtn.addActionListener(new startactionlistner());
        buttonbox.add(startbtn);
        JButton stopbtn=new JButton("stop");
        stopbtn.addActionListener(new stopactionlistner());
        buttonbox.add(stopbtn);
        JButton uptempobtn=new JButton("uptempo");
//        uptempobtn.addActionListener(new uptempoactionlistner());
        buttonbox.add(uptempobtn);
        JButton downtempobtn=new JButton("downtempo");
//        downtempobtn.addActionListener(new downtempoactionlistner());
        buttonbox.add(downtempobtn);
        JButton serializebtn=new JButton("serialize_it");
        serializebtn.addActionListener(new serializeactionlistner());
        buttonbox.add(serializebtn);
        JButton deserializebtn=new JButton("deserialize_it");
        deserializebtn.addActionListener(new deserializeactionlistner());
        buttonbox.add(deserializebtn);
        Box Namebox=new Box(BoxLayout.Y_AXIS);

        for(int i=0;i<16;i++)
        {
            Namebox.add(new Label(instrument_names[i]));
        }
        background.add(BorderLayout.EAST,buttonbox);
        background.add(BorderLayout.WEST,Namebox);
        frame.getContentPane().add(background);
        GridLayout grid=new GridLayout(16,16);
        grid.setHgap(2);
        grid.setVgap(1);
        mainpanel=new JPanel(grid);
        background.add(BorderLayout.CENTER,mainpanel);
        for(int i=0;i<256;i++)
        {
            JCheckBox checkbox=new JCheckBox();
             checkbox.setSelected(false);
             checkboxArrayList.add(checkbox);
             mainpanel.add(checkbox);
        }

        frame.setBounds(20,20,300,300);
        frame.pack();
        frame.setVisible(true);
        setupMidi();
    }
    public class serializeactionlistner implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser filesave=new JFileChooser();
            filesave.showSaveDialog(frame);
            savefile(filesave.getSelectedFile());
        }
    }
    private  void savefile(File file){
        boolean[] checkboxstate=new boolean[256];
           for(int i=0;i<256;i++)
           {
               if(checkboxArrayList.get(i).isSelected())
               {
                   checkboxstate[i]=true;
               }else{
                   checkboxstate[i]=false;
               }
           }
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(checkboxstate);
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public class deserializeactionlistner implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileload=new JFileChooser();
            fileload.showOpenDialog(frame);
            loadfile(fileload.getSelectedFile());
        }
    }

    private void loadfile(File file)
    {
//        checkboxArrayList.clear();
        boolean[] checkboxstate=null;
        try{
            FileInputStream fs=new FileInputStream(file);
            ObjectInputStream objectInputStream=new ObjectInputStream(fs);
            checkboxstate=(boolean[])objectInputStream.readObject();
        }catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i=0;i<256;i++)
        {
            if(checkboxstate[i])
            {
                checkboxArrayList.get(i).setSelected(true);
            }else{
                checkboxArrayList.get(i).setSelected(false);
            }
        }
        player.stop();
    }



    public void  setupMidi()
    {
        try{
            player=MidiSystem.getSequencer();
            player.open();
            seq=new Sequence(Sequence.PPQ,4);
            track=seq.createTrack();
            player.setTempoInBPM(120);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void buildtrack_and_start(){
        int[] tracklist;
//        System.out.println(instruments.length);
        seq.deleteTrack(track);
        track=seq.createTrack();
        for(int i=0;i<16;i++)
        {
            tracklist=new int[16];
            int key=instruments[i];
            for(int j=0;j<16;j++)
            {
                JCheckBox ckbx=checkboxArrayList.get(j + (16*i));
                if(ckbx.isSelected())
                {
                    tracklist[j]=key;
                }else{
                    tracklist[j]=0;
                }
            }
            maketrack(tracklist);
            track.add(makeevent(176,9,127,0,16));
        }
        track.add(makeevent(192,9,1,0,15));
        try{
           player.setSequence(seq);
           player.setLoopCount(player.LOOP_CONTINUOUSLY);
           player.start();
           player.setTempoInBPM(120);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void maketrack(int[] list)
    {

        for(int i=0;i<16;i++)
        {
            int key=list[i];
            track.add(makeevent(144,9,key,100,i));
            track.add(makeevent(128,9,key,100,i+1));
        }
    }

    public class startactionlistner implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {

            buildtrack_and_start();
        }
    }

    public class stopactionlistner implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {

            player.stop();
        }
    }
// making event for the track
    public static MidiEvent makeevent(int cmd, int chnl, int data1, int data2, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(cmd, chnl, data1, data2);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }
}
