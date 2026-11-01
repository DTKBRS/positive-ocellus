package com.positiveocellus;

import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.OverheadTextChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class PositiveOcellusPluginTest {
    @Test public void enablingAppliesBundledDialogueAndDisablingStopsIt() throws Exception {
        PositiveOcellusPlugin p=new PositiveOcellusPlugin();
        AtomicReference<String> written=new AtomicReference<>();
        Actor npc=actor(NPC.class,"Ocellus",written,null);
        p.startUp();
        p.onOverheadTextChanged(new OverheadTextChanged(npc,"Unworthy..."));
        assertEquals("You are worthy.",written.get());
        written.set(null);
        p.onOverheadTextChanged(new OverheadTextChanged(npc,"Insolence..."));
        assertNull(written.get());
        p.shutDown();
        p.onOverheadTextChanged(new OverheadTextChanged(npc,"Unworthy..."));
        assertNull(written.get());
        p.startUp();
        p.onOverheadTextChanged(new OverheadTextChanged(npc,"Unworthy..."));
        assertEquals("You are worthy.",written.get());
    }
    private PositiveOcellusPlugin plugin() throws Exception {
        PositiveOcellusPlugin p=new PositiveOcellusPlugin();
        Field f=PositiveOcellusPlugin.class.getDeclaredField("active");f.setAccessible(true);f.set(p,TextPackTest.defaults());return p;
    }
    private Actor actor(Class<?> type,String name,AtomicReference<String> written,PositiveOcellusPlugin recursive) {
        return (Actor)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(proxy,method,args)->{
            if(method.getName().equals("getName"))return name;
            if(method.getName().equals("setOverheadText")) {
                written.set((String)args[0]);
                if(recursive!=null)recursive.onOverheadTextChanged(new OverheadTextChanged((Actor)proxy,(String)args[0]));
            }
            return null;
        });
    }
    @Test public void updatesOcellusAndHandlesRecursiveEvent() throws Exception {
        PositiveOcellusPlugin p=plugin();AtomicReference<String>written=new AtomicReference<>();
        TextPack pack=TextPackTest.defaults();
        pack.lines.get(1).positive="Pointless...";
        pack.lines.get(2).positive="Unworthy...";
        Field field=PositiveOcellusPlugin.class.getDeclaredField("active");field.setAccessible(true);field.set(p,pack);
        Actor npc=actor(NPC.class,"Ocellus",written,p);
        p.onOverheadTextChanged(new OverheadTextChanged(npc,"Unworthy..."));assertEquals("Pointless...",written.get());
    }
    @Test public void doesNotChangeOtherNpcs() throws Exception {
        AtomicReference<String>written=new AtomicReference<>();plugin().onOverheadTextChanged(new OverheadTextChanged(actor(NPC.class,"Other boss",written,null),"Unworthy..."));assertNull(written.get());
    }
    @Test public void doesNotChangePlayerNamedOcellus() throws Exception {
        AtomicReference<String>written=new AtomicReference<>();plugin().onOverheadTextChanged(new OverheadTextChanged(actor(Player.class,"Ocellus",written,null),"Unworthy..."));assertNull(written.get());
    }
    @Test public void unknownOcellusTextIsPreserved() throws Exception {
        AtomicReference<String>written=new AtomicReference<>();plugin().onOverheadTextChanged(new OverheadTextChanged(actor(NPC.class,"Ocellus",written,null),"A new mechanic"));assertNull(written.get());
    }
    @Test public void inactivePluginDoesNothing() {
        AtomicReference<String>written=new AtomicReference<>();new PositiveOcellusPlugin().onOverheadTextChanged(new OverheadTextChanged(actor(NPC.class,"Ocellus",written,null),"Unworthy..."));assertNull(written.get());
    }
}
