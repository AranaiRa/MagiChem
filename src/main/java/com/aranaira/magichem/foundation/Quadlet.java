package com.aranaira.magichem.foundation;

public class Quadlet<F, S, T, H> implements IQuadlet<F, S, T, H> {
    private F first;
    private S second;
    private T third;
    private H fourth;

    public Quadlet(F pFirst, S pSecond, T pThird, H pFourth) {
        this.first = pFirst;
        this.second = pSecond;
        this.third = pThird;
        this.fourth = pFourth;
    }

    @Override
    public F getFirst() {
        return first;
    }

    @Override
    public S getSecond() {
        return second;
    }

    @Override
    public T getThird() {
        return third;
    }

    @Override
    public H getFourth() {
        return fourth;
    }

}

interface IQuadlet<F, S, T, H> {
    public F getFirst();
    public S getSecond();
    public T getThird();
    public H getFourth();
}