package hf.thewalkinglife.model;

/**
 * The class representing the main entity of the application, as well as a row in the database.
 */
public class StepData {
    public String date;
    public int stepCount;

    public StepData(String date, int stepCount) {
        this.date = date;
        this.stepCount = stepCount;
    }
}
