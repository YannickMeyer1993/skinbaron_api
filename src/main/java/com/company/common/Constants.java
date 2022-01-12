package com.company.common;

import java.util.ArrayList;

public class Constants {
    public static final String INV_TYPE_SKINBARON_SALES = "skinbaron sales";
    public static final String INV_TYPE_steam = "steam";
    public static final String INV_TYPE_storage = "storage";
    public static final String INV_TYPE_skinbaron = "skinbaron";
    public static final String INV_TYPE_smurf = "smurf";
    public static final ArrayList<String> DOPPLER_PHASES = new ArrayList<>();
    public static final ArrayList<String> LIST_FOR_NEWEST_SALES = new ArrayList<>();

    static {
        DOPPLER_PHASES.add("1");
        DOPPLER_PHASES.add("2");
        DOPPLER_PHASES.add("3");
        DOPPLER_PHASES.add("4");
        DOPPLER_PHASES.add("Ruby");
        DOPPLER_PHASES.add("Sapphire");
        DOPPLER_PHASES.add("Black Pearl");

        LIST_FOR_NEWEST_SALES.add("ase");//Case
        LIST_FOR_NEWEST_SALES.add("att");//Battle-Scarred
        LIST_FOR_NEWEST_SALES.add("act");//fn
        LIST_FOR_NEWEST_SALES.add("Min");//mw
        LIST_FOR_NEWEST_SALES.add("iel");//ft
        LIST_FOR_NEWEST_SALES.add("ell");//ww
        LIST_FOR_NEWEST_SALES.add("ick");//Sticker
        LIST_FOR_NEWEST_SALES.add("atc");//Patch
        LIST_FOR_NEWEST_SALES.add("age");//package
        LIST_FOR_NEWEST_SALES.add("Cap");//capsule
        LIST_FOR_NEWEST_SALES.add("tat");//Stattrak
        LIST_FOR_NEWEST_SALES.add("Pin");//Pin
        LIST_FOR_NEWEST_SALES.add("tat");//Stattrak
        LIST_FOR_NEWEST_SALES.add("usi");//Music Kit
        LIST_FOR_NEWEST_SALES.add("nif");//Knife
        LIST_FOR_NEWEST_SALES.add("olo");//Holo
        LIST_FOR_NEWEST_SALES.add("per");//Opeartion
    }
}
