package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.route.UnicastCandidates;

import java.util.*;

public class UnicastCandidatesSortingMethods {
    record DSTv2Holder(double deadline, double mbps, UnicastCandidates uc){
    }

    public static Map<UnicastCandidates, Double> assignDToUC(List<UnicastCandidates> avbCandidateRoutes) {
        Map<UnicastCandidates, Double> ucTrafficMap = new HashMap<>();
        for (UnicastCandidates uc : avbCandidateRoutes) {
            ucTrafficMap.put(uc, uc.getApplication().getDeadline());
        }
        return ucTrafficMap;
    }

    public static Map<UnicastCandidates, Double> assignDSTToUC(List<UnicastCandidates> avbCandidateRoutes) {
        Map<UnicastCandidates, Double> ucTrafficMap = new HashMap<>();
        for (UnicastCandidates uc : avbCandidateRoutes) {
            ucTrafficMap.put(uc, (double) (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / uc.getApplication().getCMI() / uc.getApplication().getDeadline());
        }
        return ucTrafficMap;
    }

    public static List<DSTv2Holder> assignDSTv2ToUC(List<UnicastCandidates> avbCandidateRoutes) {
        List<DSTv2Holder> dsTv2HolderList = new ArrayList<>();
        for (UnicastCandidates uc : avbCandidateRoutes) {
            dsTv2HolderList.add(new DSTv2Holder(uc.getApplication().getDeadline(), (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI(), uc));
        }
        return dsTv2HolderList;
    }

    public static LinkedHashMap<UnicastCandidates, Double> sortUCByDeadline(Map<UnicastCandidates, Double> ucDeadlineMap){
        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = new LinkedHashMap<>();
        ucDeadlineMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedUCByDeadline.put(x.getKey(), x.getValue()));

        return sortedUCByDeadline;
    }

    public static LinkedHashMap<UnicastCandidates, Double> sortUCByDST(Map<UnicastCandidates, Double> ucDSTMap){
        LinkedHashMap<UnicastCandidates, Double> sortedUCByDST = new LinkedHashMap<>();
        ucDSTMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedUCByDST.put(x.getKey(), x.getValue()));

        return sortedUCByDST;
    }

    public static List<UnicastCandidates> sortUCByDSTv2(List<DSTv2Holder> dsTv2HolderList){
        Comparator<DSTv2Holder> comparator = Comparator
                .comparingDouble((DSTv2Holder dsTv2Holder) -> dsTv2Holder.deadline)
                .thenComparingDouble((DSTv2Holder dsTv2Holder) -> -dsTv2Holder.mbps);

        dsTv2HolderList.sort(comparator);

        List<UnicastCandidates> sortedUCByDST = new ArrayList<>();

        for(DSTv2Holder dsTv2Holder: dsTv2HolderList){
            sortedUCByDST.add(dsTv2Holder.uc);
        }

        return sortedUCByDST;
    }
}
