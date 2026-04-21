package myst.synthetic.page;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PageValue(@Nullable Float scalar, List<Float> vector, @Nullable String text) {

    public static final Codec<PageValue> CODEC = Codec.either(
            Codec.FLOAT,
            Codec.either(Codec.FLOAT.listOf(), Codec.STRING)
    ).comapFlatMap(
            either -> {
                if (either.left().isPresent()) {
                    Float scalar = either.left().get();
                    if (scalar == null || !Float.isFinite(scalar)) {
                        return DataResult.error(() -> "Invalid scalar page value");
                    }
                    return DataResult.success(PageValue.scalar(scalar));
                }

                Either<List<Float>, String> nested = either.right().orElseThrow();

                if (nested.left().isPresent()) {
                    List<Float> vector = nested.left().get();
                    if (vector.size() != 3) {
                        return DataResult.error(() -> "Vector page value must contain exactly 3 floats");
                    }

                    for (Float component : vector) {
                        if (component == null || !Float.isFinite(component)) {
                            return DataResult.error(() -> "Invalid vector page value component");
                        }
                    }

                    return DataResult.success(PageValue.vector(vector.get(0), vector.get(1), vector.get(2)));
                }

                String text = nested.right().orElse(null);
                if (text == null || text.isBlank()) {
                    return DataResult.error(() -> "Invalid text page value");
                }

                return DataResult.success(PageValue.text(text));
            },
            value -> {
                if (value.isScalar()) {
                    return Either.left(value.scalar());
                }

                if (value.isVector()) {
                    return Either.right(Either.left(value.vector()));
                }

                return Either.right(Either.right(value.text()));
            }
    );

    public PageValue {
        vector = vector == null ? List.of() : List.copyOf(vector);

        if (scalar != null && !Float.isFinite(scalar)) {
            scalar = null;
        }

        if (text != null && text.isBlank()) {
            text = null;
        }

        int modes = 0;
        if (scalar != null) {
            modes++;
        }
        if (!vector.isEmpty()) {
            modes++;
        }
        if (text != null) {
            modes++;
        }

        if (modes > 1) {
            throw new IllegalArgumentException("PageValue can only hold one value mode at a time");
        }

        if (!vector.isEmpty()) {
            if (vector.size() != 3) {
                throw new IllegalArgumentException("Vector page value must contain exactly 3 floats");
            }

            for (Float component : vector) {
                if (component == null || !Float.isFinite(component)) {
                    throw new IllegalArgumentException("Vector page value contains invalid component");
                }
            }
        }
    }

    public static PageValue scalar(float scalar) {
        return new PageValue(scalar, List.of(), null);
    }

    public static PageValue vector(float x, float y, float z) {
        return new PageValue(null, List.of(x, y, z), null);
    }

    public static PageValue text(String text) {
        return new PageValue(null, List.of(), text);
    }

    public boolean isScalar() {
        return scalar != null;
    }

    public boolean isVector() {
        return !vector.isEmpty();
    }

    public boolean isText() {
        return text != null;
    }

    @Nullable
    public Float scalarOrNull() {
        return scalar;
    }

    @Nullable
    public List<Float> vectorOrNull() {
        return vector.isEmpty() ? null : vector;
    }

    @Nullable
    public String textOrNull() {
        return text;
    }
}