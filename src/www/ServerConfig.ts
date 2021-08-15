export default interface ServerConfig {
	log?: LogObject;
	api?: ApiObject;
	dns?: DnsObject;
	stats?: StatsObject;
	policy?: PolicyObject;
	routing?: RoutingObject;
	inbounds?: InboundObject[];
	outbounds?: OutboundObject[];
	transport?: TransportObject;
}

export interface LogObject {
	access: string,
	error: string,
	loglevel: "debug" | "info" | "warning" | "error" | "none";
}

export interface ApiObject {
	tag: string;
	services: Array<"HandlerService" | "LoggerService" | "StatsService">;
}

export interface DnsObject {
	hosts?: {
		[key: string]: string;
	};
	servers?: Array<string | ServerObject>;
	clientIp?: string;
	tag?: string;
}

export interface ServerObject {
	address: string;
	port: number;
	domains: string[];
}

export interface StatsObject { }

export interface PolicyObject {
	levels: {
		[key: string]: LevelPolicyObject;
	};
	system?: SystemPolicyObject;
}

export interface LevelPolicyObject {
	handshake?: number;
	connIdle?: number;
	uplinkOnly?: number;
	downlinkOnly?: number;
	statsUserUplink?: boolean;
	statsUserDownlink?: boolean;
	bufferSize?: number;

}

export interface SystemPolicyObject {
	statsInboundUplink?: boolean;
	statsInboundDownlink?: boolean;
}

export interface RoutingObject {
	domainStrategy?: "AsIs" | "IPIfNonMatch" | "IPOnDemand";
	rules?: RuleObject[];
	balancers?: BalancerObject[];
}

export interface RuleObject {
	type?: "field";
	domain?: string[];
	ip?: string[];
	port?: number | string;
	network?: "tcp" | "udp" | "tcp,udp";
	source?: string[];
	user?: string[];
	inboundTag?: string[];
	outboundTag?: string;
	protocol?: Array<"http" | "tls" | "bittorrent">;
	attrs?: string;
	balancerTag?: string;
}

export interface BalancerObject {
	tag?: string;
	selector?: string[];

}

export interface SniffingObject {
	enabled: boolean;
	destOverride: Array<"http" | "tls">;
}

export interface AllocateObject {
	strategy: "always" | "random";
	refresh?: number;
	concurrency?: number;
}

interface InboundObject {
	port: number | string;
	listen?: string;
	protocol: string;
	settings?: InboundConfigurationObject;
	streamSettings?: StreamSettingsObject[];
	tag?: string;
	sniffing?: SniffingObject;
	allocate?: AllocateObject;
}
interface OutboundObject {
	sendThrough?: string;
	protocol: string;
	settings: OutboundConfigurationObject;
	tag?: string;
	streamSettings?: StreamSettingsObject;
	proxySettings?: ProxySettingsObject;
	mux?: MuxObject;
}

export interface BlackholeOutboundObject extends OutboundObject {
	protocol: "backhole";
	settings: BlackholeOutboundObject;
}
export interface DNSOutboundObject extends OutboundObject {
	protocol: "dns";
	settings: DNSOutboundObject;
}
export interface FreedomOutboundObject extends OutboundObject {
	protocol: "freedom";
	settings: FreedomOutboundObject;
}
export interface DokodemoInboundObject extends InboundObject {
	protocol: "dokodemo-door";
	settings: DokodemoInboundConfigurationObject;
}
export interface HttpInboundObject extends InboundObject {
	protocol: "http";
	settings: HttpInboundConfigurationObject;
}
export interface MTProtoInboundObject extends InboundObject {
	protocol: "mtproto";
	settings: MTProtoInboundConfigurationObject;
}
export interface MTProtoOutboundObject extends OutboundObject {
	protocol: "mtproto";
	settings: MTProtoOutboundConfigurationObject;
}
export interface ShadowsocksInboundObject extends InboundObject {
	protocol: "shadowsocks";
	settings: ShadowsocksInboundConfigurationObject;
}
export interface ShadowsocksOutboundObject extends OutboundObject {
	protocol: "shadowsocks";
	settings: ShadowsocksOutboundConfigurationObject;
}
export interface SocksInboundObject extends InboundObject {
	protocol: "socks";
	settings: SocksInboundConfigurationObject;
}
export interface SocksOutboundObject extends OutboundObject {
	protocol: "socks";
	settings: SocksOutboundConfigurationObject;
}
export interface VmessInboundObject extends InboundObject {
	protocol: "vmess";
	settings: VMessInboundConfigurationObject;
}
export interface VmessOutboundObject extends OutboundObject {
	protocol: "vmess";
	settings: VMessOutboundConfigurationObject;
}


export interface ProxySettingsObject {
	tag: string;
}

export interface MuxObject {
	enabled: boolean;
	concurrency: number;
}

export interface TransportObject {
	tcpSettings?: TcpObject;
	kcpSettings?: KcpObject;
	wsSettings?: WebSocketObject;
	httpSettings?: HttpObject;
	dsSettings?: DomainSocketObject;
	quicSettings?: QUICObject;
}

export interface StreamSettingsObject {
	network?: "tcp" | "kcp" | "ws" | "http" | "domainsocket" | "quic";
	security?: "none" | "tls";
	tlsSettings?: TLSObject;
	tcpSettings?: TcpObject;
	kcpSettings?: KcpObject;
	wsSettings?: WebSocketObject;
	httpSettings?: HttpObject;
	dsSettings?: DomainSocketObject;
	quicSettings?: QUICObject;
	sockopt?: SockoptObject;
}

export interface TLSObject {
	serverName?: string;
	alpn?: string[];
	allowInsecure?: boolean;
	allowInsecureCiphers?: boolean;
	disableSystemRoot?: boolean;
	certificates: CertificateObject[];

}
export interface CertificateObject {
	usage?: "encipherment" | "verify" | "issue";
	certificateFile?: string;
	certificate?: string[];
	keyFile?: string;
	key?: string;

}

export interface SockoptObject {
	mark?: number;
	tcpFastOpen?: boolean;
	tproxy?: boolean;
}

export interface TcpObject {
	header: NoneHeaderObject | HttpHeaderobject;
}

export interface NoneHeaderObject {
	type: "none";
}

export interface HttpHeaderobject {
	type: "http";
	request?: HTTPRequestObject;
	response?: HTTPResponseObject;
}

export interface HTTPRequestObject {
	version?: string;
	method?: string;
	path?: string[];
	headers?: {
		[key: string]: string[];
	};
}

export interface HTTPResponseObject {
	version?: string;
	status?: string;
	reason?: string;
	headers?: {
		[key: string]: string;
	};
}

export interface KcpObject {
	mtu?: number;
	tti?: number;
	uplinkCapacity?: number;
	downlinkCapacity?: number;
	readBufferSize?: number;
	writeBufferSize?: number;
	header?: HeaderObject;
}

export interface HeaderObject {
	type?: "none" | "srtp" | "utp" | "wechat-video" | "dtls" | "wireguard";
}

export interface WebSocketObject {
	path: string;
	headers?: {
		[key: string]: string;
	};
}

export interface HttpObject {
	host?: string[];
	path?: string;
}

export interface DomainSocketObject {
	path: string;
}

export interface QUICObject {
	security?: "none" | "aes-128-gcm" | "chacha20-poly1305";
	key?: string;
	header?: HeaderObject;
}

interface OutboundConfigurationObject { }
interface InboundConfigurationObject { }

export interface BlackholeOutboundConfigurationObject extends OutboundConfigurationObject {
	response?: {
		type?: "none" | "http";
	};
}

export interface DNSOutboundConfigurationObject extends OutboundConfigurationObject {
	network?: "tcp" | "udp";
	address?: string;
	port?: number;
}
export interface FreedomOutboundConfigurationObject extends OutboundConfigurationObject {
	domainStrategy?: "AsIs" | "UseIP" | "UseIPv4" | "UseIPv6";
	redirect?: string;
	userLevel?: number;
}

export interface DokodemoInboundConfigurationObject extends InboundConfigurationObject {
	address?: string;
	port?: number;
	network?: "tcp" | "udp" | "tcp,udp";
	followRedirect?: boolean;
	userLevel?: number;
}

export interface MTProtoInboundConfigurationObject extends InboundConfigurationObject {
	users: MTProtoUserObject[];
}
export interface MTProtoOutboundConfigurationObject extends OutboundConfigurationObject { }

export interface MTProtoUserObject {
	email: string;
	level?: number;
	secret: string;
}


export interface HttpInboundConfigurationObject extends InboundConfigurationObject {
	accounts: AccountObject[];
	allowTransparent?: boolean;
	userLevel?: number;
}

export interface AccountObject {
	user: string;
	pass: string;
}

export interface ShadowsocksInboundConfigurationObject extends InboundConfigurationObject {
	email: string;
	method: string;
	password: string;
	level?: number;
	ota?: boolean;
	network: "tcp" | "udp" | "tcp,udp";
}

export interface ShadowsocksOutboundConfigurationObject extends OutboundConfigurationObject {
	servers: ShadowsocksServerObject[];
}

export interface ShadowsocksServerObject {
	email: string;
	address: string;
	port: number;
	method: string;
	password: string;
	level?: number;
	ota?: boolean;
}

export interface SocksInboundConfigurationObject extends InboundConfigurationObject {
	auth?: "noauth" | "password";
	accounts?: AccountObject[];
	ip?: string;
	userLevel?: number;
	udp?: boolean;
}

export interface SocksOutboundConfigurationObject extends OutboundConfigurationObject {
	servers: SocksServerObject[];
}

export interface SocksServerObject {
	address: string;
	port: number;
	users?: SocksUserObject[];
}

export interface SocksUserObject {
	user: string;
	pass: string;
	level?: number;
}

export interface VMessInboundConfigurationObject extends InboundConfigurationObject {
	clients?: VmessClientObject[];
	detour?: DetourObject;
	default?: DefaultObject;
	disableInsecureEncryption?: boolean;
}

export interface VMessOutboundConfigurationObject extends OutboundConfigurationObject {
	vnext: VmessServerObject[];
}

export interface VmessServerObject {
	address: string;
	port: number;
	users: VmessUserObject[];
}

export interface VmessUserObject {
	id: string;
	alterId: number;
	level: number;
	security: "aes-128-gcm" | "chacha20-poly1305" | "auto" | "none";
}

export interface VmessClientObject {
	id: string;
	alterId: number;
	email: string;
	level: number;

}

export interface DetourObject {
	to: string;
}

export interface DefaultObject {
	level?: number;
	alterId?: number;
}
