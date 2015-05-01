
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author volka_000
 */
public class human {
    
    String gene;
    double fitness;
    
public human (String gene){

this.gene=gene;
this.fitness=0;

}

public double getFitness() {
		return this.fitness;
	}
public human(){

    this.fitness=0.0;
}

public static Comparator<human> fitnessComp = new Comparator<human>() {
        @Override
        public int compare(human o1, human o2) {
        if (o1.getFitness() < o2.getFitness()) return -1;
        if (o1.getFitness() > o2.getFitness()) return 1;
        return 0;}};
    
}
