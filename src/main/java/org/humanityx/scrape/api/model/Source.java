package org.humanityx.scrape.api.model;

import org.humanityx.util.Hash;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;

/**
 * A source is a web page (web retrievable text) that is identified by and url.
 * @author Arvid Halma
 * @version 8-5-2015 - 21:27
 */
public class Source {

    @NotNull
    @JsonProperty
    private long id;

    @JsonProperty
    private Long baseId;

    @NotNull
    @JsonProperty
    private String url;

    @JsonProperty
    private String md5;

    @JsonProperty
    private DateTime created;

    @JsonProperty
    private String lang;

    @JsonProperty
    private int type;

    public Source() {
        this.baseId = null;
        this.created = new DateTime();
    }

    public Source(String url) {
        this();
        this.url = url;
        this.md5 = Hash.md5(url);
    }

    public long getId() {
        return id;
    }

    public Source setId(long id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Source setUrl(String url) {
        this.url = url;
        return this;
    }

    public DateTime getCreated() {
        return created;
    }

    public Source setCreated(DateTime created) {
        this.created = created;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public Source setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public int getType() {
        return type;
    }

    public Source setType(int type) {
        this.type = type;
        return this;
    }

    public Long getBaseId() {
        return baseId;
    }

    public Source setBaseId(Long baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getMd5() {
        return md5;
    }

    public Source setMd5(String md5) {
        this.md5 = md5;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source)) return false;

        Source source = (Source) o;

        return id == source.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Source{" +
                "id=" + id +
                ", baseId=" + baseId +
                ", url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", created=" + created +
                ", lang='" + lang + '\'' +
                ", type=" + type +
                '}';
    }
}
