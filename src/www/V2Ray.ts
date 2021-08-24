import V2RayConfig from "./V2RayConfig";
import ServerConfig from "./ServerConfig";

export enum VPNStatus {
	STARTING = 0,
	SETUPING = 1,
	STARTED = 2,
	STOPING = 3,
	STOPPED = 4,
}
type StatusListener = (status: VPNStatus) => any;
export default class V2Ray {
	public VPNStatus = VPNStatus;
	private statusListeners: StatusListener[] = [];
	private initStatusWatcher = false;

	public connect(config: V2RayConfig, serverConfig: ServerConfig): Promise<void> {
		return new Promise((resolve, reject) => {
			window.cordova.exec(() => resolve(), reject, "V2Ray", "start", [config, serverConfig]);
		});
	}

	public disconnect(): Promise<void> {
		return new Promise((resolve, reject) => {
			window.cordova.exec(() => resolve(), reject, "V2Ray", "stop");
		});
	}

	public getStatus(): Promise<VPNStatus> {
		return new Promise((resolve, reject) => {
			window.cordova.exec(resolve, reject, "V2Ray", "status");
		});
	}

	public queryStats(tag: string, direct: string): Promise<number> {
		return new Promise((resolve, reject) => {
			window.cordova.exec(resolve, reject, "V2Ray", "query-stats", [tag, direct]);
		});
	}

	public prepare(): Promise<void> {
		return new Promise((resolve, reject) => {
			window.cordova.exec(() => resolve(), reject, "V2Ray", "prepare");
		});
	}

	public addStatusListener(callback: StatusListener) {
		this.statusListeners.push(callback);
		if (!this.initStatusWatcher) {
			window.cordova.exec((status) => {
				this.statusListeners.forEach((listener) => listener(status));
			}, () => {}, "V2Ray", "watch-status");
			this.initStatusWatcher = true;
		}
	}

	public removeStatusListener(callback: StatusListener) {
		const i = this.statusListeners.indexOf(callback);
		if (i >= 0) {
			this.statusListeners.splice(i, 1);
		}
	}
}