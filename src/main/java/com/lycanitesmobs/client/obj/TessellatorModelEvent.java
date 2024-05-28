package com.lycanitesmobs.client.obj;

import net.minecraftforge.eventbus.api.Event;

public class TessellatorModelEvent extends Event
{

    public static class RenderPre extends TessellatorModelEvent
    {
        public RenderPre(ObjModel model)
        {
            super(model);
        }
    }

    public static class RenderPost extends TessellatorModelEvent
    {
        public RenderPost(ObjModel model)
        {
            super(model);
        }
    }

    public ObjModel model;

    public TessellatorModelEvent(ObjModel model)
    {
        this.model = model;
    }

    public static class RenderGroupEvent extends TessellatorModelEvent
    {

        public String group;

        public RenderGroupEvent(String groupName, ObjModel model)
        {
            super(model);
            this.group = groupName;
        }

        public static class Pre extends RenderGroupEvent
        {
            public Pre(String g, ObjModel m)
            {
                super(g, m);
            }
        }

        public static class Post extends RenderGroupEvent
        {
            public Post(String g, ObjModel m)
            {
                super(g, m);
            }
        }

    }

    public boolean isCancelable()
    {
        return true;
    }

}
