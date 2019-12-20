/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.core.InternalApi;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.*;

public class Binding {
    private String role;
    private List<String> members;
    private Condition condition;

    public static class Builder {
        private List<String> members = new ArrayList();
        private String role;
        private Condition condition;

        @InternalApi("This class should only be extended within google-cloud-java")
        protected Builder() {
        }

        @InternalApi("This class should only be extended within google-cloud-java")
        protected Builder(Binding binding) {
            setRole(binding.role);
            setMembers(binding.members);
            setCondition(binding.condition);
        }

        public final Binding.Builder setRole(String role) {
            String nullIdentityMessage = "The role cannot be null.";
            checkNotNull(role, nullIdentityMessage);
            this.role = role;
            return this;
        }

        public final Binding.Builder setMembers(List<String> members) {
            String nullIdentityMessage = "Null members are not permitted.";
            checkNotNull(members, nullIdentityMessage);
            this.members.clear();
            for (String member : members) {
                // Check member not null
                this.members.add(member);
            }
            return this;
        }

        public final Binding.Builder removeMembers(String first, String... others) {
            String nullIdentityMessage = "Null members are not permitted.";
            checkNotNull(first, nullIdentityMessage);
            checkNotNull(others, nullIdentityMessage);
            this.members.remove(first);
            for (String member : others) {
                this.members.remove(member);
            }
            return this;
        }

        public final Binding.Builder addMembers(String first, String... others) {
            String nullIdentityMessage = "Null identities are not permitted.";
            checkNotNull(first, nullIdentityMessage);
            checkNotNull(others, nullIdentityMessage);
            this.members.add(first);
            for (String member : others) {
                this.members.add(member);
            }
            return this;
        }

        public final Binding.Builder setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Creates a {@code Policy} object.
         */
        public final Binding build() {
            return new Binding(this);
        }
    }

    private Binding(Binding.Builder builder) {
        this.role = builder.role;
        this.members = ImmutableList.copyOf(builder.members);
        this.condition = builder.condition;
    }

    public Binding.Builder toBuilder() {
        return new Binding.Builder(this);
    }

    public String getRole() {
        return this.role;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public Condition getCondition() {
        return this.condition;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("role", role)
                .add("members", members)
                .add("condition", condition)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), role, members, condition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Binding)) {
            return false;
        }
        Binding other = (Binding) obj;
        return Objects.equals(role, other.getRole())
                && Objects.equals(members, other.getMembers())
                && Objects.equals(condition, other.getCondition());
    }

    /**
     * Returns a builder for {@code Policy} objects.
     */
    public static Binding.Builder newBuilder() {
        return new Binding.Builder();
    }
}
