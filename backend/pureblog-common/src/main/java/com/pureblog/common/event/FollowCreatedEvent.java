package com.pureblog.common.event;

public record FollowCreatedEvent(
        Long followerId,
        Long followingId
) {
}
