package com.medievaltd.research;

import java.util.List;

public class ResearchNode {
    public final ResearchId id;
    public final String name;
    public final String description;
    public final int cost;
    public final List<ResearchId> requires;
    public final float treeX;
    public final float treeY;

    public ResearchNode(ResearchId id, String name, String description,
                        int cost, List<ResearchId> requires,
                        float treeX, float treeY) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.requires = requires;
        this.treeX = treeX;
        this.treeY = treeY;
    }
}
