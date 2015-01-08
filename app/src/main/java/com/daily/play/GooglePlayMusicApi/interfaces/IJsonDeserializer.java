package com.daily.play.GooglePlayMusicApi.interfaces;

public interface IJsonDeserializer
{
    <T> T deserialize(String data, Class<T> clazz);
}