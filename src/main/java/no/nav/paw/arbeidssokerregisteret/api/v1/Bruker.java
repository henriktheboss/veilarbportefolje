/**
 * Autogenerated by Avro
 * <p>
 * DO NOT EDIT DIRECTLY
 */
package no.nav.paw.arbeidssokerregisteret.api.v1;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;

/**
 * En bruker er en person eller et system. Personer kan være sluttbrukere eller veiledere.
 */
@org.apache.avro.specific.AvroGenerated
public class Bruker extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    private static final long serialVersionUID = -7230641963271372109L;


    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Bruker\",\"namespace\":\"no.nav.paw.arbeidssokerregisteret.api.v1\",\"doc\":\"En bruker er en person eller et system. Personer kan være sluttbrukere eller veiledere.\",\"fields\":[{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"BrukerType\",\"symbols\":[\"UKJENT_VERDI\",\"UDEFINERT\",\"VEILEDER\",\"SYSTEM\",\"SLUTTBRUKER\"],\"default\":\"UKJENT_VERDI\"},\"doc\":\"Angir hvilken type bruker det er snakk om\"},{\"name\":\"id\",\"type\":\"string\",\"doc\":\"Brukerens identifikator.\\nFor sluttbruker er dette typisk fødselsnummer eller D-nummer.\\nFor system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).\\nFor veileder vil det være NAV identen til veilederen.\"}]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    private static final SpecificData MODEL$ = new SpecificData();

    private static final BinaryMessageEncoder<Bruker> ENCODER =
            new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

    private static final BinaryMessageDecoder<Bruker> DECODER =
            new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

    /**
     * Return the BinaryMessageEncoder instance used by this class.
     *
     * @return the message encoder used by this class
     */
    public static BinaryMessageEncoder<Bruker> getEncoder() {
        return ENCODER;
    }

    /**
     * Return the BinaryMessageDecoder instance used by this class.
     *
     * @return the message decoder used by this class
     */
    public static BinaryMessageDecoder<Bruker> getDecoder() {
        return DECODER;
    }

    /**
     * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
     *
     * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
     * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
     */
    public static BinaryMessageDecoder<Bruker> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
    }

    /**
     * Serializes this Bruker to a ByteBuffer.
     *
     * @return a buffer holding the serialized data for this instance
     * @throws java.io.IOException if this instance could not be serialized
     */
    public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
        return ENCODER.encode(this);
    }

    /**
     * Deserializes a Bruker from a ByteBuffer.
     *
     * @param b a byte buffer holding serialized data for an instance of this class
     * @return a Bruker instance decoded from the given buffer
     * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
     */
    public static Bruker fromByteBuffer(
            java.nio.ByteBuffer b) throws java.io.IOException {
        return DECODER.decode(b);
    }

    /**
     * Angir hvilken type bruker det er snakk om
     */
    private BrukerType type;
    /**
     * Brukerens identifikator.
     * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
     * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
     * For veileder vil det være NAV identen til veilederen.
     */
    private CharSequence id;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public Bruker() {
    }

    /**
     * All-args constructor.
     *
     * @param type Angir hvilken type bruker det er snakk om
     * @param id   Brukerens identifikator.
     *             For sluttbruker er dette typisk fødselsnummer eller D-nummer.
     *             For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
     *             For veileder vil det være NAV identen til veilederen.
     */
    public Bruker(BrukerType type, CharSequence id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public SpecificData getSpecificData() {
        return MODEL$;
    }

    @Override
    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    @Override
    public Object get(int field$) {
        switch (field$) {
            case 0:
                return type;
            case 1:
                return id;
            default:
                throw new IndexOutOfBoundsException("Invalid index: " + field$);
        }
    }

    // Used by DatumReader.  Applications should not call.
    @Override
    @SuppressWarnings(value = "unchecked")
    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                type = (BrukerType) value$;
                break;
            case 1:
                id = (CharSequence) value$;
                break;
            default:
                throw new IndexOutOfBoundsException("Invalid index: " + field$);
        }
    }

    /**
     * Gets the value of the 'type' field.
     *
     * @return Angir hvilken type bruker det er snakk om
     */
    public BrukerType getType() {
        return type;
    }


    /**
     * Sets the value of the 'type' field.
     * Angir hvilken type bruker det er snakk om
     *
     * @param value the value to set.
     */
    public void setType(BrukerType value) {
        this.type = value;
    }

    /**
     * Gets the value of the 'id' field.
     *
     * @return Brukerens identifikator.
     * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
     * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
     * For veileder vil det være NAV identen til veilederen.
     */
    public CharSequence getId() {
        return id;
    }


    /**
     * Sets the value of the 'id' field.
     * Brukerens identifikator.
     * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
     * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
     * For veileder vil det være NAV identen til veilederen.
     *
     * @param value the value to set.
     */
    public void setId(CharSequence value) {
        this.id = value;
    }

    /**
     * Creates a new Bruker RecordBuilder.
     *
     * @return A new Bruker RecordBuilder
     */
    public static Bruker.Builder newBuilder() {
        return new Bruker.Builder();
    }

    /**
     * Creates a new Bruker RecordBuilder by copying an existing Builder.
     *
     * @param other The existing builder to copy.
     * @return A new Bruker RecordBuilder
     */
    public static Bruker.Builder newBuilder(Bruker.Builder other) {
        if (other == null) {
            return new Bruker.Builder();
        } else {
            return new Bruker.Builder(other);
        }
    }

    /**
     * Creates a new Bruker RecordBuilder by copying an existing Bruker instance.
     *
     * @param other The existing instance to copy.
     * @return A new Bruker RecordBuilder
     */
    public static Bruker.Builder newBuilder(Bruker other) {
        if (other == null) {
            return new Bruker.Builder();
        } else {
            return new Bruker.Builder(other);
        }
    }

    /**
     * RecordBuilder for Bruker instances.
     */
    @org.apache.avro.specific.AvroGenerated
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Bruker>
            implements org.apache.avro.data.RecordBuilder<Bruker> {

        /**
         * Angir hvilken type bruker det er snakk om
         */
        private BrukerType type;
        /**
         * Brukerens identifikator.
         * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
         * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
         * For veileder vil det være NAV identen til veilederen.
         */
        private CharSequence id;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(SCHEMA$, MODEL$);
        }

        /**
         * Creates a Builder by copying an existing Builder.
         *
         * @param other The existing Builder to copy.
         */
        private Builder(Bruker.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.type)) {
                this.type = data().deepCopy(fields()[0].schema(), other.type);
                fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }
            if (isValidValue(fields()[1], other.id)) {
                this.id = data().deepCopy(fields()[1].schema(), other.id);
                fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }
        }

        /**
         * Creates a Builder by copying an existing Bruker instance
         *
         * @param other The existing instance to copy.
         */
        private Builder(Bruker other) {
            super(SCHEMA$, MODEL$);
            if (isValidValue(fields()[0], other.type)) {
                this.type = data().deepCopy(fields()[0].schema(), other.type);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.id)) {
                this.id = data().deepCopy(fields()[1].schema(), other.id);
                fieldSetFlags()[1] = true;
            }
        }

        /**
         * Gets the value of the 'type' field.
         * Angir hvilken type bruker det er snakk om
         *
         * @return The value.
         */
        public BrukerType getType() {
            return type;
        }


        /**
         * Sets the value of the 'type' field.
         * Angir hvilken type bruker det er snakk om
         *
         * @param value The value of 'type'.
         * @return This builder.
         */
        public Bruker.Builder setType(BrukerType value) {
            validate(fields()[0], value);
            this.type = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'type' field has been set.
         * Angir hvilken type bruker det er snakk om
         *
         * @return True if the 'type' field has been set, false otherwise.
         */
        public boolean hasType() {
            return fieldSetFlags()[0];
        }


        /**
         * Clears the value of the 'type' field.
         * Angir hvilken type bruker det er snakk om
         *
         * @return This builder.
         */
        public Bruker.Builder clearType() {
            type = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'id' field.
         * Brukerens identifikator.
         * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
         * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
         * For veileder vil det være NAV identen til veilederen.
         *
         * @return The value.
         */
        public CharSequence getId() {
            return id;
        }


        /**
         * Sets the value of the 'id' field.
         * Brukerens identifikator.
         * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
         * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
         * For veileder vil det være NAV identen til veilederen.
         *
         * @param value The value of 'id'.
         * @return This builder.
         */
        public Bruker.Builder setId(CharSequence value) {
            validate(fields()[1], value);
            this.id = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'id' field has been set.
         * Brukerens identifikator.
         * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
         * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
         * For veileder vil det være NAV identen til veilederen.
         *
         * @return True if the 'id' field has been set, false otherwise.
         */
        public boolean hasId() {
            return fieldSetFlags()[1];
        }


        /**
         * Clears the value of the 'id' field.
         * Brukerens identifikator.
         * For sluttbruker er dette typisk fødselsnummer eller D-nummer.
         * For system vil det rett og slett være navnet på et system, eventuelt med versjonsnummer i tillegg (APP_NAVN:VERSJON).
         * For veileder vil det være NAV identen til veilederen.
         *
         * @return This builder.
         */
        public Bruker.Builder clearId() {
            id = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Bruker build() {
            try {
                Bruker record = new Bruker();
                record.type = fieldSetFlags()[0] ? this.type : (BrukerType) defaultValue(fields()[0]);
                record.id = fieldSetFlags()[1] ? this.id : (CharSequence) defaultValue(fields()[1]);
                return record;
            } catch (org.apache.avro.AvroMissingFieldException e) {
                throw e;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumWriter<Bruker>
            WRITER$ = (org.apache.avro.io.DatumWriter<Bruker>) MODEL$.createDatumWriter(SCHEMA$);

    @Override
    public void writeExternal(java.io.ObjectOutput out)
            throws java.io.IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumReader<Bruker>
            READER$ = (org.apache.avro.io.DatumReader<Bruker>) MODEL$.createDatumReader(SCHEMA$);

    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    @Override
    protected boolean hasCustomCoders() {
        return true;
    }

    @Override
    public void customEncode(org.apache.avro.io.Encoder out)
            throws java.io.IOException {
        out.writeEnum(this.type.ordinal());

        out.writeString(this.id);

    }

    @Override
    public void customDecode(org.apache.avro.io.ResolvingDecoder in)
            throws java.io.IOException {
        org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.type = BrukerType.values()[in.readEnum()];

            this.id = in.readString(this.id instanceof Utf8 ? (Utf8) this.id : null);

        } else {
            for (int i = 0; i < 2; i++) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.type = BrukerType.values()[in.readEnum()];
                        break;

                    case 1:
                        this.id = in.readString(this.id instanceof Utf8 ? (Utf8) this.id : null);
                        break;

                    default:
                        throw new java.io.IOException("Corrupt ResolvingDecoder.");
                }
            }
        }
    }
}










