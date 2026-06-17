package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.data.MinanceSavedData;
import com.fluorineuck.minance.entity.village.LoadedVillageScan;
import com.fluorineuck.minance.entity.village.VillageService;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VillageCompanyService {
    public static final VillageCompanyService INSTANCE = new VillageCompanyService();

    private VillageCompanyService() {
    }

    public Map<String, VillageCandidate> candidates() {
        return VillageService.INSTANCE.candidates();
    }

    public Map<String, VillageCompany> companies() {
        return CompanyService.INSTANCE.companies();
    }

    public void replaceState(Map<String, VillageCandidate> loadedCandidates, Map<String, VillageCompany> loadedCompanies) {
        VillageService.INSTANCE.replaceCandidates(loadedCandidates);
        CompanyService.INSTANCE.replaceCompanies(loadedCompanies);
    }

    public void scanLoadedVillages(MinecraftServer server, long gameTime) {
        MinanceSavedData.get(server);
        CompanyConfig config = ConfigRegistry.INSTANCE.company();
        Map<String, LoadedVillageScan> scans = VillageService.INSTANCE.scanLoadedVillages(server, config);
        List<VillageCandidate> readyCandidates = VillageService.INSTANCE.updateCandidates(scans, CompanyService.INSTANCE.companies().keySet(), config);
        Set<String> registered = CompanyService.INSTANCE.registerReadyCompanies(readyCandidates, scans, config, gameTime);
        for (String id : registered) {
            VillageService.INSTANCE.removeCandidate(id);
        }
        CompanyService.INSTANCE.updateCompaniesFromScans(scans, registered, config, gameTime);
        if (!scans.isEmpty()) {
            MinanceSavedData.get(server).setDirty();
        }
    }

    public List<VillageCompany> sortedCompanies() {
        return CompanyService.INSTANCE.sortedCompanies();
    }

    public List<VillageCandidate> sortedCandidates() {
        return VillageService.INSTANCE.sortedCandidates();
    }
}
