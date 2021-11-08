package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.util.ArrayList;
import javax.swing.JTree;

public class SlotService implements ResourceService  {
    public static final int[] HEADERS = { 0x534c5462 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        ArrayList<Slot> slots = entry.getResource("slots");
        if (slots == null) {
            Resource resource = new Resource(data);
            resource.decompress(true);
            try { 
                int count = resource.i32();
                slots = new ArrayList<Slot>(count);
                for (int i = 0; i < count; ++i) {
                    Slot slot = new Slot(resource, true, false);
                    slots.add(slot);
                    if (slot.root != null) {
                        FileEntry levelEntry = Globals.findEntry(slot.root);
                        if (levelEntry != null) {
                            levelEntry.revision = resource.revision;
                            levelEntry.setResource("slot", slot);
                        }
                    }
                }
                entry.setResource("slots", slots);
            }
            catch (Exception e) { System.err.println("There was an error processing RSlotList file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}