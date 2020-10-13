package net.sistr.lmml.util;

import net.sistr.lmml.entity.IHasMultiModel;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * アーマー4部位の保持クラス
 * */
public class ArmorsHolder<T> {
    private final Object[] parts = new Object[4];

    public void setArmor(T type, IHasMultiModel.Part part) {
        parts[part.getIndex()] = type;
    }

    @SuppressWarnings("unchecked")
    public Optional<T> getArmor(@Nonnull IHasMultiModel.Part part) {
        return Optional.ofNullable((T) parts[part.getIndex()]);
    }

    public <M> ArmorsHolder<M> convert(Function<T, M> converter) {
        ArmorsHolder<M> armorsHolder = new ArmorsHolder<>();
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            this.getArmor(part).ifPresent(type -> armorsHolder.setArmor(converter.apply(type), part));
        }
        return armorsHolder;
    }

    public boolean isEmpty() {
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            if (this.getArmor(part).isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArmorsHolder<?> that = (ArmorsHolder<?>) o;
        return Arrays.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }
}
