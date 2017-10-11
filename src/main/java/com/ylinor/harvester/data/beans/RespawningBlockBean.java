package com.ylinor.harvester.data.beans;

public class RespawningBlockBean {
    /** Database id **/
    private int id;
    /** X position of block **/
    private int x;
    /** Y position of block **/
    private int y;
    /** Z position of block **/
    private int z;
    /** Type of the block **/
    private String block_type;
    /** Timestamp when block must respawn **/
    private int respawnTime;

    public RespawningBlockBean(int x, int y, int z, String block_type, int respawnTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block_type = block_type;
        this.respawnTime = respawnTime;
    }
    public RespawningBlockBean(int id, int x, int y, int z, String block_type, int respawnTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block_type = block_type;
        this.respawnTime = respawnTime;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }
    public void setZ(int z) {
        this.z = z;
    }

    public String getBlockType() {
        return block_type;
    }
    public void setBlockType(String block_type) {
        this.block_type = block_type;
    }

    public int getRespawnTime() {
        return respawnTime;
    }
    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }
}
