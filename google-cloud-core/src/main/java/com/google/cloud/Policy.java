/*
 * Copyright 2016 Google LLC
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.core.ApiFunction;
import com.google.api.core.InternalApi;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class for Identity and Access Management (IAM) policies. IAM policies are used to specify access
 * settings for Cloud Platform resources. A policy is a map of bindings. A binding assigns a set of
 * identities to a role, where the identities can be user accounts, Google groups, Google domains,
 * and service accounts. A role is a named list of permissions defined by IAM.
 *
 * @see <a href="https://cloud.google.com/iam/reference/rest/v1/Policy">Policy</a>
 */
public final class Policy implements Serializable {

  private static final long serialVersionUID = -3348914530232544290L;
  private final List<Binding> bindingsV3;
  private final String etag;
  private final int version;

  public abstract static class Marshaller<T> {

    @InternalApi("This class should only be extended within google-cloud-java")
    protected Marshaller() {}

    protected static final ApiFunction<String, Identity> IDENTITY_VALUE_OF_FUNCTION =
        new ApiFunction<String, Identity>() {
          @Override
          public Identity apply(String identityPb) {
            return Identity.valueOf(identityPb);
          }
        };
    protected static final ApiFunction<Identity, String> IDENTITY_STR_VALUE_FUNCTION =
        new ApiFunction<Identity, String>() {
          @Override
          public String apply(Identity identity) {
            return identity.strValue();
          }
        };

    protected abstract Policy fromPb(T policyPb);

    protected abstract T toPb(Policy policy);
  }

  public static class DefaultMarshaller extends Marshaller<com.google.iam.v1.Policy> {

    @Override
    protected Policy fromPb(com.google.iam.v1.Policy policyPb) {
        List<Binding> bindingsV3 = new ArrayList<Binding>();
        for (com.google.iam.v1.Binding bindingPb : policyPb.getBindingsList()) {
          bindingsV3.add(
                  Binding.newBuilder().setRole(Role.of(bindingPb.getRole()))
                          .setIdentities(ImmutableSet.copyOf(
                                  Lists.transform(
                                          bindingPb.getMembersList(),
                                          new Function<String, Identity>() {
                                            @Override
                                            public Identity apply(String s) {
                                              return IDENTITY_VALUE_OF_FUNCTION.apply(s);
                                            }
                                          })))
                          .setCondition(null)
                          .build());
          // TODO(frankyn): Add support for bindingBuilder.setCondition after com.google.iam.v1 is regenerated.

        }
        return newBuilder()
                .setBindingsV3(bindingsV3)
                .setEtag(
                        policyPb.getEtag().isEmpty()
                                ? null
                                : BaseEncoding.base64().encode(policyPb.getEtag().toByteArray()))
                .setVersion(policyPb.getVersion())
                .build();
    }

    @Override
    protected com.google.iam.v1.Policy toPb(Policy policy) {
      com.google.iam.v1.Policy.Builder policyBuilder = com.google.iam.v1.Policy.newBuilder();
      List<com.google.iam.v1.Binding> bindingPbList = new LinkedList<>();
      for (Binding binding : policy.getBindingsV3()) {
        com.google.iam.v1.Binding.Builder bindingBuilder = com.google.iam.v1.Binding.newBuilder();
        bindingBuilder.setRole(binding.getRole().getValue());
        bindingBuilder.addAllMembers(
                Lists.transform(
                        new ArrayList<>(binding.getIdentities()),
                        new Function<Identity, String>() {
                          @Override
                          public String apply(Identity identity) {
                            return IDENTITY_STR_VALUE_FUNCTION.apply(identity);
                          }
                        }));
        // TODO(frankyn): Add support for bindingBuilder.setCondition after com.google.iam.v1 is regenerated.
        bindingPbList.add(bindingBuilder.build());
      }
      policyBuilder.addAllBindings(bindingPbList);
      if (policy.etag != null) {
        policyBuilder.setEtag(ByteString.copyFrom(BaseEncoding.base64().decode(policy.etag)));
      }
      policyBuilder.setVersion(policy.version);
      return policyBuilder.build();
    }
  }

  /** A builder for {@code Policy} objects. */
  public static class Builder {
    private final List<Binding> bindingsV3 = new ArrayList();
    private String etag;
    private int version;

    @InternalApi("This class should only be extended within google-cloud-java")
    protected Builder() {}

    @InternalApi("This class should only be extended within google-cloud-java")
    protected Builder(Policy policy) {
      setBindingsV3(policy.bindingsV3);
      setEtag(policy.etag);
      setVersion(policy.version);
    }

    /**
     * Replaces the builder's map of bindings with the given map of bindings.
     *
     * @throws NullPointerException if the given map is null or contains any null keys or values
     * @throws IllegalArgumentException if any identities in the given map are null
     * @throws IllegalArgumentException if policy version is equal to 3.
     */
    public final Builder setBindings(Map<Role, Set<Identity>> bindings) {
      checkNotNull(bindings, "The provided map of bindings cannot be null.");
      for (Map.Entry<Role, Set<Identity>> binding : bindings.entrySet()) {
        checkNotNull(binding.getKey(), "The role cannot be null.");
        Set<Identity> identities = binding.getValue();
        checkNotNull(identities, "A role cannot be assigned to a null set of identities.");
        checkArgument(!identities.contains(null), "Null identities are not permitted.");
      }
      // convert into v3 format
      this.bindingsV3.clear();
      for (Map.Entry<Role, Set<Identity>> binding : bindings.entrySet()) {
        Binding.Builder bindingBuilder = Binding.newBuilder();
        bindingBuilder.setIdentities(new HashSet<>(binding.getValue()));
        bindingBuilder.setRole(binding.getKey());
        this.bindingsV3.add(bindingBuilder.build());
      }
      return this;
    }

    /**
     * Replaces the builder's map of bindings with the given map of bindingsV3.
     *
     * @throws NullPointerException if the given map is null or contains any null keys or values
     * @throws IllegalArgumentException if any identities in the given map are null
     */
    public final Builder setBindingsV3(List<Binding> bindings) {
      for (Binding binding : bindings) {
        checkNotNull(binding.getRole().getValue(), "The role cannot be null.");
        Set<Identity> identities = binding.getIdentities();
        checkNotNull(identities, "A role cannot be assigned to a null set of identities.");
        checkArgument(!identities.contains(null), "Null identities are not permitted.");
      }
      // Set version to 3.
      this.bindingsV3.clear();
      for (Binding binding : bindings) {
        Binding.Builder bindingBuilder = Binding.newBuilder();
        bindingBuilder.setIdentities(new HashSet<>(binding.getIdentities()));
        bindingBuilder.setRole(binding.getRole());
        bindingBuilder.setCondition(binding.getCondition());
        this.bindingsV3.add(bindingBuilder.build());
      }
      return this;
    }


    /** Removes the role (and all identities associated with that role) from the policy. */
    public final Builder removeRole(Role role) {
      checkArgument(this.version != 3, "removeRole is not supported with version 3 policies.");
      for (Binding binding : bindingsV3) {
        if (binding.getRole().equals(role)) {
          bindingsV3.remove(binding);
        }
      }

      return this;
    }

    /**
     * Adds one or more identities to the policy under the role specified.
     *
     * @throws NullPointerException if the role or any of the identities is null.
     * @throws IllegalArgumentException if policy version is equal to 3.
     */
    public final Builder addIdentity(Role role, Identity first, Identity... others) {
      checkArgument(this.version != 3, "removeRole is not supported with version 3 policies.");
      String nullIdentityMessage = "Null identities are not permitted.";
      checkNotNull(first, nullIdentityMessage);
      checkNotNull(others, nullIdentityMessage);
      for (Binding binding : bindingsV3) {
        if (binding.getRole().equals(checkNotNull(role, "The role cannot be null."))) {
          Binding.Builder bindingBuilder = binding.toBuilder();
          bindingBuilder.addIdentity(first, others);
          bindingsV3.remove(binding);
          bindingsV3.add(bindingBuilder.build());
          return this;
        }
      }

      Binding.Builder bindingBuilder = Binding.newBuilder();
      bindingBuilder.setRole(role);
      bindingBuilder.addIdentity(first, others);
      bindingsV3.add(bindingBuilder.build());

      return this;
    }

    /**
     * Removes one or more identities from an existing binding. Does nothing if the binding
     * associated with the provided role doesn't exist.
     * @throws IllegalArgumentException if policy version is equal to 3.
     */
    public final Builder removeIdentity(Role role, Identity first, Identity... others) {
      checkArgument(this.version != 3, "removeRole is not supported with version 3 policies.");
      for (Binding binding : bindingsV3) {
        if (binding.getRole().equals(checkNotNull(role, "The role cannot be null."))) {
          Binding.Builder bindingBuilder = binding.toBuilder();
          bindingBuilder.removeIdentity(first, others);
          bindingsV3.remove(binding);
          Binding builtBinding = bindingBuilder.build();
          if (builtBinding.getIdentities() != null && !builtBinding.getIdentities().isEmpty()) {
            bindingsV3.add(builtBinding);
          }
          break;
        }
      }
      return this;
    }

    /**
     * Sets the policy's etag.
     *
     * <p>Etags are used for optimistic concurrency control as a way to help prevent simultaneous
     * updates of a policy from overwriting each other. It is strongly suggested that systems make
     * use of the etag in the read-modify-write cycle to perform policy updates in order to avoid
     * race conditions. An etag is returned in the response to getIamPolicy, and systems are
     * expected to put that etag in the request to setIamPolicy to ensure that their change will be
     * applied to the same version of the policy. If no etag is provided in the call to
     * setIamPolicy, then the existing policy is overwritten blindly.
     */
    public final Builder setEtag(String etag) {
      this.etag = etag;
      return this;
    }

    /**
     * Sets the version of the policy. The default version is 0, meaning only the "owner", "editor",
     * and "viewer" roles are permitted. If the version is 1, you may also use other roles.
     */
    public final Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    /** Creates a {@code Policy} object. */
    public final Policy build() {
      return new Policy(this);
    }
  }

  private Policy(Builder builder) {
    ImmutableList.Builder<Binding> bindingsV3Builder = ImmutableList.builder();
    for (Binding binding : builder.bindingsV3) {
      Binding.Builder bindingBuilder = Binding.newBuilder();
      bindingBuilder.setRole(binding.getRole())
              .setIdentities(ImmutableSet.copyOf(binding.getIdentities()))
              .setCondition(binding.getCondition());
      bindingsV3Builder.add(bindingBuilder.build());
    }
    this.bindingsV3 = bindingsV3Builder.build();
    this.etag = builder.etag;
    this.version = builder.version;
  }

  /** Returns a builder containing the properties of this IAM Policy. */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Returns the map of bindings that comprises the policy.
   *
   * @throws IllegalArgumentException if policy version is equal to 3.
   * */

  public Map<Role, Set<Identity>> getBindings() {
    checkArgument(this.version != 3, "removeRole is not supported with version 3 policies.");
    // Convert to V1 IAM Policy if version is not 3.
    ImmutableMap.Builder<Role, Set<Identity>> bindingsV1Builder = ImmutableMap.builder();
    for (Binding binding : bindingsV3) {
      bindingsV1Builder.put(binding.getRole(), binding.getIdentities());
    }
    return bindingsV1Builder.build();
  }

  /** Returns the map of bindings that comprises the policy for version 3. */

  public List<Binding> getBindingsV3() {
    return bindingsV3;
  }

  /**
   * Returns the policy's etag.
   *
   * <p>Etags are used for optimistic concurrency control as a way to help prevent simultaneous
   * updates of a policy from overwriting each other. It is strongly suggested that systems make use
   * of the etag in the read-modify-write cycle to perform policy updates in order to avoid race
   * conditions. An etag is returned in the response to getIamPolicy, and systems are expected to
   * put that etag in the request to setIamPolicy to ensure that their change will be applied to the
   * same version of the policy. If no etag is provided in the call to setIamPolicy, then the
   * existing policy is overwritten blindly.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * Returns the version of the policy. The default version is 0, meaning only the "owner",
   * "editor", and "viewer" roles are permitted. If the version is 1, you may also use other roles.
   */
  public int getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("bindings", bindingsV3)
        .add("etag", etag)
        .add("version", version)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), bindingsV3, etag, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Policy)) {
      return false;
    }
    Policy other = (Policy) obj;
    return Objects.equals(bindingsV3, other.getBindings())
        && Objects.equals(etag, other.getEtag())
        && Objects.equals(version, other.getVersion());
  }

  /** Returns a builder for {@code Policy} objects. */
  public static Builder newBuilder() {
    return new Builder();
  }
}
