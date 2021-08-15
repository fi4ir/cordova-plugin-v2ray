import V2Ray from "./V2Ray";

declare global {
	interface Window {
		v2ray: V2Ray;
	}
}

export = new V2Ray();
