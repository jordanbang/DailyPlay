package com.jb.dailyplay.GooglePlayMusicApi.interfaces;

public interface IJsonDeserializer
{
    <T> T deserialize(String data, Class<T> clazz);
}