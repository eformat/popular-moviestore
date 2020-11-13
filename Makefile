# Image URL to use all building/pushing image targets
REGISTRY ?= quay.io
REPOSITORY ?= $(REGISTRY)/eformat/popular-moviestore

IMG := $(REPOSITORY):latest

# clean compile
compile:
	mvn clean package -DskipTests

# Podman Login
podman-login:
	podman login -u $(DOCKER_USER) -p $(DOCKER_PASSWORD) $(REGISTRY)

# Build the oci image no compile
podman-build-nocompile:
	podman build . -t ${IMG} -f src/main/docker/Dockerfile.jvm

# Build the oci image
podman-build: compile
	podman build . -t ${IMG} -f src/main/docker/Dockerfile.jvm

# Push the oci image
podman-push: podman-build
	podman push ${IMG}

# Push the oci image
podman-push-nocompile: podman-build-nocompile
	podman push ${IMG}

# Just Push the oci image
podman-push-nobuild:
	podman push ${IMG}

podman-run:
	podman-compose -f docker-compose.yaml up -d

podman-stop:
	podman-compose -f docker-compose.yaml down

# Cert-utils operator
CERTUTILS_NAMESPACE ?= cert-utils-operator

define deploy-certutil-operator
oc new-project "${CERTUTILS_NAMESPACE}" || true
helm repo add cert-utils-operator https://redhat-cop.github.io/cert-utils-operator
helm repo update
$(eval cert_utils_chart_version := $(shell helm search repo cert-utils-operator/cert-utils-operator | grep cert-utils-operator/cert-utils-operator | awk '{print $$2}'))
helm fetch cert-utils-operator/cert-utils-operator --version ${cert_utils_chart_version}
helm template cert-utils-operator-${cert_utils_chart_version}.tgz --namespace cert-utils-operator | oc apply -f - -n cert-utils-operator
rm -f cert-utils-operator-${cert_utils_chart_version}.tgz
oc -n "${CERTUTILS_NAMESPACE}" wait --for condition=available --timeout=120s deployment/cert-utils-operator
sleep 10
endef

define undeploy-certutil-operator
oc delete project "${CERTUTILS_NAMESPACE}" || true
oc delete project "${CERTUTILS_NAMESPACE}" || true
endef

deploy-certutil-operator:
	$(call deploy-certutil-operator)

undeploy-certutil-operator:
	$(call undeploy-certutil-operator)

#Infinispan
INFINISPAN_NAMESPACE ?= popular-moviestore

# Datagrid using OLM
define deploy-infinispan-operator
oc new-project "${INFINISPAN_NAMESPACE}" || true
oc -n "${INFINISPAN_NAMESPACE}" apply -f ocp/infinispan-operatorgroup.yaml
oc -n "${INFINISPAN_NAMESPACE}" apply -f ocp/infinispan-subscription.yaml
sleep 60
oc -n "${INFINISPAN_NAMESPACE}" wait --for condition=available --timeout=120s deployment/infinispan-operator
endef

define undeploy-infinispan-operator
$(eval csv := $(shell oc -n "$${INFINISPAN_NAMESPACE}" get subscription datagrid -o jsonpath='{.status.installedCSV}'))
oc -n "${INFINISPAN_NAMESPACE}" delete -f ocp/infinispan-subscription.yaml || true
oc -n "${INFINISPAN_NAMESPACE}" delete -f ocp/infinispan-operatorgroup.yaml || true
oc -n "${INFINISPAN_NAMESPACE}" delete csv ${csv} || true
oc delete project "${INFINISPAN_NAMESPACE}" || true
endef

# Datagrid instance
define deploy-infinispan
oc -n "${INFINISPAN_NAMESPACE}" apply -f ocp/infinispan-auth-secret.yaml
oc -n "${INFINISPAN_NAMESPACE}" apply -f ocp/infinispan-cr.yaml
endef

define undeploy-infinispan
oc -n "${INFINISPAN_NAMESPACE}" delete -f ocp/infinispan-cr.yaml || true
oc -n "${INFINISPAN_NAMESPACE}" delete -f ocp/infinispan-auth-secret.yaml || true
endef

deploy-infinispan-operator:
	$(call deploy-infinispan-operator)

undeploy-infinispan-operator:
	$(call undeploy-infinispan-operator)

deploy-infinispan:
	$(call deploy-infinispan)

undeploy-infinispan:
	$(call undeploy-infinispan)


# Istio using OLM
ISTIO_NAMESPACE ?= istio-system

define deploy-istio-control-plane
oc new-project "${ISTIO_NAMESPACE}" || true
oc apply -f ocp/kiali-subscription.yaml
oc apply -f ocp/jaeger-subscription.yaml
oc apply -f ocp/istio-subscription.yaml
sleep 60
oc -n openshift-operators wait --for condition=available --timeout=200s deployment/istio-operator
endef

define undeploy-istio-control-plane
$(eval kcsv := $(shell oc -n openshift-operators get subscription kiali-ossm -o jsonpath='{.status.installedCSV}'))
oc delete -f ocp/kiali-subscription.yaml || true
oc delete csv ${kcsv} -n openshift-operators --cascade=true || true
$(eval jcsv := $(shell oc -n openshift-operators get subscription jaeger-product -o jsonpath='{.status.installedCSV}'))
oc delete -f ocp/jaeger-subscription.yaml || true
oc delete csv ${jcsv} -n openshift-operators --cascade=true || true
$(eval icsv := $(shell oc -n openshift-operators get subscription servicemeshoperator -o jsonpath='{.status.installedCSV}'))
oc delete -f ocp/istio-subscription.yaml || true
oc delete csv ${icsv} -n openshift-operators --cascade=true || true
oc delete project "${ISTIO_NAMESPACE}" || true
endef

define deploy-istio-mesh
oc -n "${ISTIO_NAMESPACE}" apply -f ocp/istio-cr.yaml
sleep 10
oc -n ${ISTIO_NAMESPACE} wait --for condition=ready --timeout=600s smcp my-mesh
endef

define undeploy-istio-mesh
oc -n "${ISTIO_NAMESPACE}" delete -f ocp/istio-cr.yaml
endef

deploy-istio-control-plane:
	$(call deploy-istio-control-plane)

undeploy-istio-control-plane:
	$(call undeploy-istio-control-plane)

deploy-istio-mesh:
	$(call deploy-istio-mesh)

undeploy-istio-mesh:
	$(call undeploy-istio-mesh)


define deploy-app
oc new-project popular-moviestore || true
helm template my -f chart/values.yaml chart | oc apply -n popular-moviestore -f-
endef

define undeploy-app
helm template my -f chart/values.yaml chart | oc delete -n popular-moviestore -f- || true
oc delete project popular-moviestore || true
endef

define deploy-app-istio
oc new-project popular-moviestore || true
helm template my -f chart/values.yaml chart --set istio.enabled=true | oc apply -n popular-moviestore -f-
endef

define undeploy-app-istio
helm template my -f chart/values.yaml chart --set istio.enabled=true | oc delete -n popular-moviestore -f- || true
oc delete project popular-moviestore || true
endef

deploy-app:
	$(call deploy-app)

deploy-app-istio:
	$(call deploy-app-istio)

undeploy-app:
	$(call undeploy-app)

undeploy-app-istio:
	$(call undeploy-app-istio)

# All targets
deploy-all:
	$(call deploy-infinispan-operator)
	$(call deploy-infinispan)
	$(call deploy-certutil-operator)
	$(call deploy-app)

undeploy-all:
	$(call undeploy-app)
	$(call undeploy-certutil-operator)
	$(call undeploy-infinispan)
	$(call undeploy-infinispan-operator)

deploy-all-istio:
	$(call deploy-istio-control-plane)
	$(call deploy-istio-mesh)
	$(call deploy-infinispan-operator)
	$(call deploy-infinispan)
	$(call deploy-certutil-operator)
	$(call deploy-app-istio)

undeploy-all-istio:
	$(call undeploy-app-istio)
	$(call undeploy-certutil-operator)
	$(call undeploy-infinispan)
	$(call undeploy-infinispan-operator)
	$(call undeploy-istio-mesh)
	$(call undeploy-istio-control-plane)




