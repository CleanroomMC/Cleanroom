package zone.rong.mixinbooter.util;

/**
 * @since 12.0
 * @deprecated use {@link com.cleanroommc.cleanmix.ConditionalMixin}
 */
@Deprecated
public final class ConditionalMixin {

    public static ConditionalMixin of(String... configurations) {
        return new ConditionalMixin(com.cleanroommc.cleanmix.ConditionalMixin.of(configurations));
    }

    public static ConditionalMixin of(boolean condition, String... configurations) {
        return new ConditionalMixin(com.cleanroommc.cleanmix.ConditionalMixin.of(condition, configurations));
    }

    private final com.cleanroommc.cleanmix.ConditionalMixin delegate;

    private ConditionalMixin(com.cleanroommc.cleanmix.ConditionalMixin delegate) {
        this.delegate = delegate;
    }

    public ConditionalMixin option(String config, String option, boolean defaultValue) {
        this.delegate.option(config, option, defaultValue);
        return this;
    }

    public ConditionalMixin option(Class<?> config, String field) {
        this.delegate.option(config, field);
        return this;
    }

    public ConditionalMixin requires(String modId) {
        this.delegate.requires(modId);
        return this;
    }

    public ConditionalMixin conflicts(String modId) {
        this.delegate.conflicts(modId);
        return this;
    }

    public ConditionalMixin recommends(String modId) {
        this.delegate.recommends(modId);
        return this;
    }

    public ConditionalMixin discourages(String modId) {
        this.delegate.discourages(modId);
        return this;
    }

    public ConditionalMixin named(String name) {
        this.delegate.named(name);
        return this;
    }

    public ConditionalMixin version(String range) {
        this.delegate.version(range);
        return this;
    }

    public ConditionalMixin because(String reason) {
        this.delegate.because(reason);
        return this;
    }

    public ConditionalMixin orWarn() {
        this.delegate.orWarn();
        return this;
    }

    public ConditionalMixin orCrash() {
        this.delegate.orCrash();
        return this;
    }

    public boolean register() {
        return this.delegate.register();
    }

}
