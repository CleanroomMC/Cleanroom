package us.getfluxed.controlsearch.client.gui;

public enum EnumSortingType {

    DEFAULT, AZ, ZA;


    public EnumSortingType cycle() {
        return switch (this) {
            case DEFAULT -> AZ;
            case AZ -> ZA;
            default -> DEFAULT;
        };
    }

    public String getName(){
        return switch (this) {
            case AZ -> "A->Z";
            case ZA -> "Z->A";
            default -> "Default";
        };
    }

}
