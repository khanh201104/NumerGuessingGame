package model;

public class Level {
    private int levelNumber;
    private int minRange;
    private int maxRange;
    private int maxGuesses;
    private int baseScore;
    private int multiplier;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;

        if (levelNumber >= 1 && levelNumber <= 10) {
            this.minRange = 10;
            this.maxRange = 100;
            this.maxGuesses = 8;
            this.baseScore = 100;
            this.multiplier = 20;
        } else if (levelNumber >= 11 && levelNumber <= 20) {
            this.minRange = 100;
            this.maxRange = 999;
            this.maxGuesses = 10;
            this.baseScore = 200;
            this.multiplier = 35;
        } else if (levelNumber >= 21 && levelNumber <= 30) {
            this.minRange = 1000;
            this.maxRange = 9999;
            this.maxGuesses = 14;
            this.baseScore = 350;
            this.multiplier = 55;
        }
    }

    // Các Getters
    public int getLevelNumber() { return levelNumber; }
    public int getMinRange() { return minRange; }
    public int getMaxRange() { return maxRange; }
    public int getMaxGuesses() { return maxGuesses; }
    public int getBaseScore() { return baseScore; }
    public int getMultiplier() { return multiplier; }
}