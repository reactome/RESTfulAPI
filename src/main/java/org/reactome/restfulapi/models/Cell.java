package org.reactome.restfulapi.models;

import java.util.List;

@SuppressWarnings("serial")
public class Cell extends PhysicalEntity {
    private List<EntityWithAccessionedSequence> RNAMarker;
    private List<MarkerReference> markerReference;
    private Anatomy organ;
    private List<EntityWithAccessionedSequence> proteinMarker;
    private List<Taxon> species;
    private Anatomy tissue;
    private Anatomy tissueLayer;
    
    public Cell() {
        
    }

    public List<EntityWithAccessionedSequence> getRNAMarker() {
        return RNAMarker;
    }

    public void setRNAMarker(List<EntityWithAccessionedSequence> rNAMarker) {
        RNAMarker = rNAMarker;
    }

    public List<MarkerReference> getMarkerReference() {
        return markerReference;
    }

    public void setMarkerReference(List<MarkerReference> markerReference) {
        this.markerReference = markerReference;
    }

    public Anatomy getOrgan() {
        return organ;
    }

    public void setOrgan(Anatomy organ) {
        this.organ = organ;
    }

    public List<EntityWithAccessionedSequence> getProteinMarker() {
        return proteinMarker;
    }

    public void setProteinMarker(List<EntityWithAccessionedSequence> proteinMarker) {
        this.proteinMarker = proteinMarker;
    }

    public List<Taxon> getSpecies() {
        return species;
    }

    public void setSpecies(List<Taxon> species) {
        this.species = species;
    }

    public Anatomy getTissue() {
        return tissue;
    }

    public void setTissue(Anatomy tissue) {
        this.tissue = tissue;
    }

    public Anatomy getTissueLayer() {
        return tissueLayer;
    }

    public void setTissueLayer(Anatomy tissueLayer) {
        this.tissueLayer = tissueLayer;
    }

}
