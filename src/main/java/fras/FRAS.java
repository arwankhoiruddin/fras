package fras;

import mapreduce.Job;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import yarn.JobScheduler;
import yarn.Scheduler;

import java.util.*;

public class FRAS extends Scheduler implements JobScheduler {

    public LinkedList<Job> scheduleJob(LinkedList<Job> jobs) {
        String fclFile = "test.fcl";
        FIS fis = FIS.load(fclFile, true);

        if (fis == null) {
            System.err.println("Cannot load file");
        }

        Map<Integer, Double> fisValue = new HashMap<>();

        for (int i=0; i<jobs.size(); i++) {
            fis.setVariable("cpu_load", jobs.get(i).getCpuLoad());
            fis.setVariable("ram_load", jobs.get(i).getIOLoad());
            fis.setVariable("job_length", jobs.get(i).getJobLength());

            fis.evaluate();

            double priority = fis.getVariable("priority").getValue();
            fisValue.put(i, priority);
        }

        // sort the priority
        Map<Integer, Integer> sortedPriorities = sortByValueReversed(fisValue);

        LinkedList<Job> sortedJobs = new LinkedList<>();

        for (int i=0; i<sortedPriorities.size(); i++) {
            int idx = sortedPriorities.get(i);
            sortedJobs.add(jobs.get(idx));
        }
        return sortedJobs;
    }

    private Map<Integer, Double> sortByValue(Map<Integer, Double> unsortedMap) {
        List<Map.Entry<Integer, Double>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return (o1.getValue().compareTo(o2.getValue()));
            }
        });

        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        int counter = 0;
        for (Map.Entry<Integer, Double> entry: list) {
            sortedMap.put(counter++, entry.getValue());
        }
        return sortedMap;
    }

    private Map<Integer, Integer> sortByValueReversed(Map<Integer, Double> unsortedMap) {
        List<Map.Entry<Integer, Double>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return (o1.getValue().compareTo(o2.getValue()));
            }
        });

        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        int counter = 0;

        for (int i= list.size() - 1; i >= 0; i--) {
            sortedMap.put(counter++, list.get(i).getKey());
        }
        return sortedMap;
    }

}
