package com.comp.proj.api;

import java.time.Instant;

public record Item(String id, String name, Instant createdAt) {}
