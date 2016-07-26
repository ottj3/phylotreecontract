package edu.tcnj.phylotrees.mixedweight.data;

public enum DNABase {
    A(0), G(1), T(2), C(3);

    public final int value;

    DNABase(int value) {
        this.value = value;
    }

}
