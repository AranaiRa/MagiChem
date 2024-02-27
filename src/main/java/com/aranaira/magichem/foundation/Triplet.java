package com.aranaira.magichem.foundation;

public class Triplet<F, S, T> implements ITriplet<F, S, T> {
    private F first;
    private S second;
    private T third;

    public Triplet(F pFirst, S pSecond, T pThird) {
        this.first = pFirst;
        this.second = pSecond;
        this.third = pThird;
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

}
interface ITriplet<F, S, T> {
    public F getFirst();
    public S getSecond();
    public T getThird();
}