/*
 * Copyright (C) 2016
 * Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
 * Institut fuer Verkehrssystemtechnik
 * 
 * German Aerospace Center
 * Institute of Transportation Systems
 * 
 */
package de.dlr.ts.commons.javafx.mainwindow.top;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import de.dlr.ts.commons.javafx.mainwindow.top.FileMenu.RecentFilesListener;

/**
 *
 * @author @author <a href="mailto:maximiliano.bottazzi@dlr.de">Maximiliano Bottazzi</a>
 */
final class RecentFiles
{
    private final int MAX_ITEMS_COUNT = 10;
    private List<MenuItem> recentFiles = new ArrayList<>();
    private List<RecentFilesListener> listeners = new ArrayList<>();
    private static File lofsFile = null;

    
    /**
     * 
     * @param lofsFile 
     */
    public static void setLofsFile(File lofsFile)
    {
        RecentFiles.lofsFile = lofsFile;
    }
    
    /**
     * 
     */
    public RecentFiles()
    {        
        
        String tmpdir = System.getProperty("java.io.tmpdir");
        if(tmpdir != null)
        {
            if(!tmpdir.endsWith(File.separator))
                tmpdir += File.separator;
            
            lofsFile = new File(tmpdir + "lofs");
                
            if(lofsFile.exists())
            {
                Path path = Paths.get(lofsFile.getAbsolutePath());
                try
                {
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    int aa = lines.size()-1;
                    if(aa > 9)
                        aa = 9;
                    
                    for (int i = aa; i >= 0; i--)
                        this.add(lines.get(i));
                } 
                catch (IOException ex)
                {
                    System.out.println(ex);
                }
            } 
        }
    }

    /**
     * 
     */
    private void storeToDisc()
    {
        Path path = Paths.get(lofsFile.getAbsolutePath());
        
        List<String> ss = new ArrayList<>();
        
        for (MenuItem lastFile : recentFiles)
            ss.add(lastFile.getText());
        
        try
        {
            Files.write(path, ss, StandardCharsets.UTF_8);
        }
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }
    
    /**
     * 
     * @param filePath 
     */
    private void onlyAdd(String filePath)
    {
        for (Iterator<MenuItem> i = recentFiles.iterator(); i.hasNext();)
        {
            MenuItem item = i.next();
            if (item.getText().equals(filePath))
                i.remove();
        }
        
        MenuItem menuItem = new MenuItem(filePath);        
        recentFiles.add(0, menuItem);
        
        menuItem.addEventHandler(EventType.ROOT, (Event event) ->
        {
            for (RecentFilesListener listener : listeners)
                listener.event(menuItem.getText());
        });
        
        if(recentFiles.size() > MAX_ITEMS_COUNT - 1)
            recentFiles.remove(MAX_ITEMS_COUNT);
        
        for (int i = 0; i < recentFiles.size(); i++)
            recentFiles.get(i).setAccelerator(KeyCombination.keyCombination("Ctrl+" + ((i+1)%MAX_ITEMS_COUNT)));
    }

    /**
     * 
     * @param filePath 
     */
    public void add(String filePath)
    {
        onlyAdd(filePath);
        storeToDisc();
    }
    
    /**
     * 
     * @param listener 
     */
    void addListener(RecentFilesListener listener)
    {
        listeners.add(listener);
    }
    
    /**
     * 
     * @return 
     */
    public int size()
    {
        return recentFiles.size();
    }
    
    /**
     * 
     * @param enabled 
     */
    public void setEnabled(boolean enabled)
    {
        recentFiles.stream().forEach((lastFile) ->
        {
            lastFile.setDisable(!enabled);
        });
    }
    
    /**
     * 
     */
    public void close()
    {
    }
    
    /**
     * 
     * @return 
     */
    public MenuItem[] getAll()
    {
        return recentFiles.toArray(new MenuItem[recentFiles.size()]);
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    MenuItem get(int index)
    {
        return recentFiles.get(index);
    }
    
    /**
     * 
     */
    public void clear()
    {
        this.recentFiles.clear();
        storeToDisc();
    }
}
