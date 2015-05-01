
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
public class Read {
    public static DataSource trainSource;
    public static DataSource testSource;
    public static ArrayList <Double> costs = new ArrayList<Double>(); 
    
    public static void run() throws Exception
    {   
        trainSource = new DataSource("train.arff"); //read modified train file  
        testSource = new DataSource("test.arff");//read modiefied test file 
        assignSelectionCosts(); //read modified costs
        
    
        ArrayList <human> currentPopulation=new ArrayList<human>();//create home for population
        
        currentPopulation=createRandomPopulation(Structure.population,currentPopulation);//fullfill home with random population        
        System.out.println("Initial population has created.. Population : " + Structure.population );
        int generation=0;
        double max=0;
        
     while(true){
         
        System.out.println("---------------------------------------------------------------" + generation);
        
                
        int prob=(int)(Structure.population*Structure.crossOverProb);
      
        for (int i = 0; i < currentPopulation.size(); i++)
            calculateFitness(currentPopulation.get(i)); //slection + accuracy set individual fitness  
        
        //currentPopulation=rouletteSelect(currentPopulation);
        currentPopulation=rankSelection(currentPopulation);

       for (int k = 0; k < prob/2; k++) 
         {  Random random = new Random();
            int father=random.nextInt(currentPopulation.size());
            int mother=random.nextInt(currentPopulation.size()); 
            
            
            if (currentPopulation.get(father).gene.equals(currentPopulation.get(mother)) || mother==father){
            k--;
            System.out.println("Same individuals ... Changing Offspring ");
            }
            
            else  crossOver(currentPopulation.get(father),currentPopulation.get(mother));
            
         }
         
       
       
        for (int s = 0; s < currentPopulation.size(); s++) 

            currentPopulation.get(s).gene=mutate(currentPopulation.get(s).gene);
        
        
        for (int s = 0; s < currentPopulation.size(); s++) 
        {     
            calculateFitness(currentPopulation.get(s));
        
            System.out.println("Fitness : " +currentPopulation.get(s).fitness);
        
        }
 
        
    
    
        ArrayList <human> result =new ArrayList<human>();
        result = (ArrayList)currentPopulation.clone();
        sort(result);
    
        System.out.print("Max Fitness of Generation :  " + result.get(result.size()-1).fitness);
        System.out.println(" Gene  " + result.get(result.size()-1).gene);
        max=result.get(result.size()-1).fitness;
        System.out.println("Generation : " + (generation+1));
        generation++; 
    }
    
    
    }
    public static double calculateFitness(human person) throws Exception //return selection and error cost
    {
        double resultSelection =0;
        double resultClassify =0;
        
        String indexes="";
        /////////////////////////////////////////////////////////////////////////////////////////////
        for (int i = 0; i < person.gene.length(); i++) // selection cost
        {
         int index=Integer.parseInt(String.valueOf(person.gene.charAt(i)));

         resultSelection = resultSelection + (index*costs.get(i));   
        }
        
       // resultSelection=(((resultSelection*100)/76.11));
        //resultSelection=(((resultSelection*100)/76.11));
        /////////////////////////////////////////////////////////////////////////////////////////////
        
//if (resultSelection > 51) {person.fitness=0; return 0.0;}
        
        Instances train=trainSource.getDataSet();
        Instances test=testSource.getDataSet();
       
        
        for (int i = 0; i < person.gene.length(); i++) 
        {
            int index=Integer.parseInt(String.valueOf(person.gene.charAt(i)));
            if (index==0) //remove selection
            {
                indexes=indexes+String.valueOf(i+1)+",";
            }
          
        }
        
        
        
        String[] options = weka.core.Utils.splitOptions("-R "+indexes);
        
        Remove remove = new Remove();                         
        remove.setOptions(options);   
        
        remove.setInputFormat(train);                        
        Instances newTrain = Filter.useFilter(train, remove);  
        
        
        remove.setInputFormat(test);
        Instances newTest = Filter.useFilter(test, remove); 

        newTrain.setClassIndex(newTrain.numAttributes()-1);
        newTest.setClassIndex(newTrain.numAttributes()-1);
        
        String[] classifierOptions = weka.core.Utils.splitOptions("-C 0.25 -M 1");
        
        Classifier cls = new J48();
        cls.setOptions(classifierOptions);
        cls.buildClassifier(newTrain);
        
        
        Evaluation eval = new Evaluation(newTrain);
        eval.evaluateModel(cls, newTest);
        
        resultClassify=eval.incorrect();
       
        double misclass=eval.incorrect();
        person.fitness=((misclass*77)/3488)+resultSelection;
        //person.fitness=eval.pctCorrect()-resultSelection;
        //System.out.println("xs : " + ((misclass*77)/3488)+resultSelection  );
        
        System.out.println("Correctly Class  " + eval.pctCorrect());
    return person.fitness;    
    }

    private static ArrayList<human> rouletteSelect(ArrayList<human> village) throws Exception
    {
        
        double totalFitness=0;
        ArrayList <human> newVillage = new ArrayList<human>();
              
        for (int i = 0; i < village.size(); i++) 
        {
            calculateFitness(village.get(i));
            totalFitness=totalFitness+village.get(i).fitness;
        }
        
        
  Random random = new Random();

        for (int i = 0; i < village.size(); i++) {

     
            double randomNumber =totalFitness * random.nextDouble();

            double runningSum = 0;
            int index = 0;
            int lastAddedIndex = 0;
            
            while (runningSum < randomNumber) 
            {
                runningSum = runningSum+ village.get(index).fitness;
                lastAddedIndex = index;
                index++;
            }

            newVillage.add(village.get(lastAddedIndex));
 
            
            runningSum = 0;
            index = 0;
            lastAddedIndex = 0;
        }
       
        //human a =newVillage.get(mint);
        return newVillage;
    }

    private static void crossOver(human father, human mother) 
    {   

        Random random = new Random(); 
        int crossOverPoint = random.nextInt((19 - 1) + 1) + 1;//the last point of gene is out of scope(1-18)
        //DİKKATTT
        
        String fatherLeft = father.gene.substring(0, crossOverPoint);
        String fatherRight = father.gene.substring(crossOverPoint,father.gene.length());
        
  
        String motherLeft = mother.gene.substring(0, crossOverPoint); 
        String motherRight = mother.gene.substring(crossOverPoint,mother.gene.length());

        
        String newFatherCrossOvered= fatherLeft+motherRight;
        String newMotherCrossOvered= motherLeft+fatherRight;
        
        
        father.gene=newFatherCrossOvered;
        mother.gene=newMotherCrossOvered;

        
        
        
        
     
    }
    public static String mutate (String gene)
    {
        String newGene = gene;
        char[] geneChar = gene.toCharArray();
        
        double k=Structure.mutateProb * 20;
        
  
       
        Random random = new Random();
        for (int i = 0; i <k  ; i++) 
        {

          int mutatePoint=random.nextInt(gene.length());  //BURAYA DİKKAT
          if (geneChar[mutatePoint] == '1') 
              geneChar[mutatePoint]='0'; 
          else
              geneChar[mutatePoint]='1';
        
        }
        
           
        
        gene  = String.valueOf(geneChar);
       
        
    return gene;
    }
     public static void assignSelectionCosts () //cost list
    {
        
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(1.00);costs.add(1.00);
        costs.add(22.78);costs.add(11.41);
        costs.add(14.51);costs.add(11.41);
     
        
    }
     public static ArrayList<human> createRandomPopulation(int population,ArrayList<human> pop) throws Exception //starting population
    {
       
        Random randomGenerator = new Random();
        for (int j = 0; j < Structure.population; j++) 
        {   
            human baby=new human();
            String DNA="";
            
            for (int i = 0; i < 20; i++) //20 --> Attrbitures
            {   
               
                int randomInt=randomGenerator.nextInt(2);
                DNA=DNA+String.valueOf(randomInt);
               
               
               
            }
            baby.gene=DNA;
            pop.add(baby);
          
            System.out.println("Citizen "+ (j+1) + " :" + pop.get(j).gene);
        }
        
        return pop;
    }
     public static ArrayList<human> sort(ArrayList a)
     {
         Collections.sort(a, human.fitnessComp);
     return a;
     
     
     }
     
     public static ArrayList<human> rankSelection (ArrayList<human> candidate)
     {
      
         ArrayList<human> a=new ArrayList<human>();
         ArrayList<human> b=new ArrayList<human>();
         ArrayList<human> c=new ArrayList<human>();
         
         a= (ArrayList)candidate.clone();
         a= sort(a);
         
         for (int i = 0; i < a.size(); i++) 
         {
          for(int j=0; j < a.indexOf(a.get(i))+1 ;j++ )   
             
              b.add(a.get(i));
         }
         
        
          
          Random randomGenerator = new Random();
          for (int i = 0; i < candidate.size(); i++) 
          
          {
              
              int index=randomGenerator.nextInt(b.size());
              c.add(b.get(index));
              
             
          }
         
         
     
     return c;
     }
     
    }
