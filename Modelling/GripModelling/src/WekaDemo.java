import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Vector;

/**
 * A little demo java program for using WEKA.<br/>
 * Check out the Evaluation class for more details.
 *
 * @author     FracPete (fracpete at waikato dot ac dot nz)
 * @see        Evaluation
 */

public class WekaDemo {
  /** the classifier used internally */
  protected Classifier m_Classifier = null;
  
  /** the filter to use */
  protected Filter m_Filter = null;

  /** the training file */
  protected String m_TrainingFile = null;

  /** the training instances */
  protected Instances m_Training = null;

  /** for evaluating the classifier */
  protected Evaluation m_Evaluation = null;

  /**
   * initializes the demo
   */
  public WekaDemo() {
    super();
  }

  /**
   * sets the classifier to use
   * @param name        the classname of the classifier
   * @param options     the options for the classifier
   */
  public void setClassifier(String name, String[] options) throws Exception {
    m_Classifier = Classifier.forName(name, options);
  }

  /**
   * sets the filter to use
   * @param name        the classname of the filter
   * @param options     the options for the filter
   */
  public void setFilter(String name, String[] options) throws Exception {
    m_Filter = (Filter) Class.forName(name).newInstance();
    if (m_Filter instanceof OptionHandler)
      ((OptionHandler) m_Filter).setOptions(options);
  }

  /**
   * sets the file to use for training
   */
  public void setTraining(String name) throws Exception {
    m_TrainingFile = name;
    m_Training     = new Instances(
                        new BufferedReader(new FileReader(m_TrainingFile)));
    m_Training.setClassIndex(m_Training.numAttributes() - 1);
  }

  /**
   * runs 10fold CV over the training file
   */
  public void execute() throws Exception {
    // run filter
    m_Filter.setInputFormat(m_Training);
    Instances filtered = Filter.useFilter(m_Training, m_Filter);
    
    // train classifier on complete file for tree
    m_Classifier.buildClassifier(filtered);
    
    // 10fold CV with seed=1
    m_Evaluation = new Evaluation(filtered);
    m_Evaluation.crossValidateModel(
        m_Classifier, filtered, 10, m_Training.getRandomNumberGenerator(1));
  }

  /**
   * outputs some data about the classifier
   */
  public String toString() {
    StringBuffer        result;

    result = new StringBuffer();
    result.append("Weka - Demo\n===========\n\n");

    result.append("Classifier...: " 
        + m_Classifier.getClass().getName() + " " 
        + Utils.joinOptions(m_Classifier.getOptions()) + "\n");
    if (m_Filter instanceof OptionHandler)
      result.append("Filter.......: " 
          + m_Filter.getClass().getName() + " " 
          + Utils.joinOptions(((OptionHandler) m_Filter).getOptions()) + "\n");
    else
      result.append("Filter.......: "
          + m_Filter.getClass().getName() + "\n");
    result.append("Training file: " 
        + m_TrainingFile + "\n");
    result.append("\n");

    result.append(m_Classifier.toString() + "\n");
    result.append(m_Evaluation.toSummaryString() + "\n");
    try {
      result.append(m_Evaluation.toMatrixString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    try {
      result.append(m_Evaluation.toClassDetailsString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result.toString();
  }

  /**
   * returns the usage of the class
   */
  public static String usage() {
    return
        "\nusage:\n  " + WekaDemo.class.getName() 
        + "  CLASSIFIER <classname> [options] \n"
        + "  FILTER <classname> [options]\n"
        + "  DATASET <trainingfile>\n\n"
        + "e.g., \n"
        + "  java -classpath \".:weka.jar\" WekaDemo \n"
        + "    CLASSIFIER weka.classifiers.trees.J48 -U \n"
        + "    FILTER weka.filters.unsupervised.instance.Randomize \n"
        + "    DATASET iris.arff\n";
  }
  
  public static void saveModel(Classifier c, String name, File path) throws Exception {


	    ObjectOutputStream oos = null;
	    try {
	        oos = new ObjectOutputStream(
	                new FileOutputStream("/weka_models/" + name + ".model"));

	    } catch (FileNotFoundException e1) {
	        e1.printStackTrace();
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
	    oos.writeObject(c);
	    oos.flush();
	    oos.close();

	}
  
  public static Classifier loadModel(File path, String name) throws Exception {

	    Classifier classifier;

	    FileInputStream fis = new FileInputStream(path + name + ".model");
	    ObjectInputStream ois = new ObjectInputStream(fis);

	    classifier = (Classifier) ois.readObject();
	    ois.close();

	    return classifier;
	}
  
  /**
   * runs the program, the command line looks like this:<br/>
   * WekaDemo CLASSIFIER classname [options] 
   *          FILTER classname [options] 
   *          DATASET filename 
   * <br/>
   * e.g., <br/>
   *   java -classpath ".:weka.jar" WekaDemo \<br/>
   *     CLASSIFIER weka.classifiers.trees.J48 -U \<br/>
   *     FILTER weka.filters.unsupervised.instance.Randomize \<br/>
   *     DATASET iris.arff<br/>
   */
  public static void main(String[] args) throws Exception {

	  DataSource source = new DataSource("E:/GitHub/Project_Reach/Modelling/testRun.csv");
	  Instances data = source.getDataSet();
	  // setting class attribute if the data format does not provide this information
	  // For example, the XRFF format saves the class attribute information as well
	  if (data.classIndex() == -1)
	    data.setClassIndex(data.numAttributes() - 1);
      
	  // set the classifier
	    Classifier cls = (Classifier) new SMO();

	  
      
   // other options
      int seed  = 12;
      int folds = 2;

      // randomize data
      Random rand = new Random(seed);
      Instances randData = new Instances(data);
      randData.randomize(rand);
      if (randData.classAttribute().isNominal())
        randData.stratify(folds);

      // perform cross-validation
      Evaluation eval = new Evaluation(randData);
      for (int n = 0; n < folds; n++) {
        Instances train = randData.trainCV(folds, n);
        Instances test = randData.testCV(folds, n);
        // the above code is used by the StratifiedRemoveFolds filter, the
        // code below by the Explorer/Experimenter:
        // Instances train = randData.trainCV(folds, n, rand);

     // build and evaluate classifier
        Classifier clsCopy = Classifier.makeCopy(cls);
        clsCopy.buildClassifier(train);
        eval.evaluateModel(clsCopy, test);
      }

      // output evaluation
      System.out.println();
      System.out.println("=== Setup ===");
      System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
      System.out.println("Dataset: " + data.relationName());
      System.out.println("Folds: " + folds);
      System.out.println("Seed: " + seed);
      System.out.println();
      System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
   
  }
}
