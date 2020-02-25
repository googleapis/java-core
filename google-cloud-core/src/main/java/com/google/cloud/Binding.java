/*
 * Copyright 2020 Google LLC
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

import com.google.api.core.BetaApi;
import com.google.auto.value.AutoValue;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

@BetaApi("This is a Beta API is not stable yet and may change in the future.")
@AutoValue
public abstract class Binding {
  public abstract String getRole();

  public abstract ImmutableList<String> getMembers();

  @Nullable
  public abstract Condition getCondition();

  public abstract Builder toBuilder();

  public static Builder newBuilder() {
    return new AutoValue_Binding.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setRole(String role);

    public abstract Builder setMembers(List<String> members);

    public abstract Builder setCondition(Condition condition);

    public abstract String getRole();

    public abstract ImmutableList<String> getMembers();

    public abstract Condition getCondition();

    // Members property must be initialized before this method can be used.
    public Builder addMembers(String... members) {
      ImmutableList.Builder<String> membersBuilder = ImmutableList.builder();
      membersBuilder.addAll(getMembers());
      for (String member : members) {
        membersBuilder.add(member);
      }
      setMembers(membersBuilder.build());
      return this;
    }

    // Members property must be initialized before this method can be used.
    public Builder removeMembers(String... members) {
      Predicate<String> selectMembersNotInList =
          Predicates.not(Predicates.in(Arrays.asList(members)));
      Collection<String> filter = Collections2.filter(getMembers(), selectMembersNotInList);
      setMembers(ImmutableList.copyOf(filter));
      return this;
    }

    public abstract Binding build();
  }
}
