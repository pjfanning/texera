package edu.uci.ics.texera.workflow.operators.machineLearning.OneTraining;

public enum TrainingType {
    knn("knn"),
    svm("svm");

    private final String name;

    TrainingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
