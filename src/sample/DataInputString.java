package sample;

import javafx.beans.property.*;

public class DataInputString {

    private final StringProperty age;
    private final StringProperty typeCitizens;
    private final FloatProperty population;
    private final FloatProperty numberOfDeath;
    private final FloatProperty deathRate;

    public DataInputString() {
        this(null, null, 0, 0);
    }

    public DataInputString(String age, String typeCitizens, float population, float numberOfDeath) {
        this.age = new SimpleStringProperty(age);
        this.typeCitizens = new SimpleStringProperty(typeCitizens);
        this.population = new SimpleFloatProperty(population);
        this.numberOfDeath = new SimpleFloatProperty(numberOfDeath);
        this.deathRate = new SimpleFloatProperty((numberOfDeath / population) * 100);
    }

    public DataInputString(String age, String typeCitizens, float population, float numberOfDeath, float deathRate) {
        this.age = new SimpleStringProperty(age);
        this.typeCitizens = new SimpleStringProperty(typeCitizens);
        this.population = new SimpleFloatProperty(population);
        this.numberOfDeath = new SimpleFloatProperty(numberOfDeath);
        this.deathRate = new SimpleFloatProperty(deathRate);
    }

    public String getAge() {
        return age.get();
    }

    public StringProperty ageProperty() {
        return age;
    }

    public void setAge(String age) {
        this.age.set(age);
    }

    public String getTypeCitizens() {
        return typeCitizens.get();
    }

    public StringProperty typeCitizensProperty() {
        return typeCitizens;
    }

    public void setTypeCitizens(String typeCitizens) {
        this.typeCitizens.set(typeCitizens);
    }

    public float getPopulation() {
        return population.get();
    }

    public FloatProperty populationProperty() {
        return population;
    }

    public void setPopulation(float population) {
        this.population.set(population);
    }

    public float getNumberOfDeath() {
        return numberOfDeath.get();
    }

    public FloatProperty numberOfDeathProperty() {
        return numberOfDeath;
    }

    public void setNumberOfDeath(float numberOfDeath) {
        this.numberOfDeath.set(numberOfDeath);
    }

    public float getDeathRate() {
        return deathRate.get();
    }

    public FloatProperty deathRateProperty() {
        return deathRate;
    }

    public void setDeathRate(float deathRate) {
        this.deathRate.set(deathRate);
    }
}