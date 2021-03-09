package gov.alaska.gmchandheld;

import java.util.ArrayList;

public class MoveDisplayObjInstance {
    // https://stackoverflow.com/a/19620252
    static MoveDisplayObjInstance obj = null;

    private final ArrayList<String> moveList = new ArrayList<>();
    public ArrayList<String> getMoveList() {
        return moveList;
    }


    private String toDestination;
    public String getToDestination(){return toDestination;}

    public static MoveDisplayObjInstance getInstance()
    {
        if (obj == null) {
            obj = new MoveDisplayObjInstance();
        }

        return obj;
    }
    public MoveDisplayObjInstance auditDisplayObjInstance;
}