import V2RayConfig from "./V2RayConfig";
import ServerConfig from "./ServerConfig";

export default class V2Ray {
	public connect(config: V2RayConfig, serverConfig: ServerConfig) {
		(window as any).cordova.exec((data) => {
			console.log(data);
		}, (err) => {
			console.error(err);
		}, "V2RayPlugin", "start", [config, serverConfig]);
	}
}