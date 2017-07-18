package kr.co.ldcc.edu.iot;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import comus.wp.onem2m.iwf.common.M2MException;
import comus.wp.onem2m.iwf.nch.NotifyResponse;
import comus.wp.onem2m.iwf.run.CmdListener;
import comus.wp.onem2m.iwf.run.IWF;

public class SmartPlug {

	private static final Logger LOG = LoggerFactory.getLogger(SmartPlug.class);

	private IWF vDevice;
	private Runtime rt;
	private String req;

	private String on = "1";
	private String off = "0";
	private String OID = "0000000000000000_00000000";

	/**
	 * 등록
	 * @throws Exception
	 */
	private void register() throws Exception {
		try {
			vDevice = new IWF(OID);
		} catch (IOException | M2MException e) {
			e.printStackTrace();
			throw new Exception(">> 선언 오류");
		}
		vDevice.register();
	}

	/**
	 * 제어
	 * @throws Exception
	 */
	private void listen() throws Exception {
		if (vDevice != null) {
			rt = Runtime.getRuntime();
			try {
				rt.exec("gpio mode 0 out");
			} catch (IOException e) {
				e.printStackTrace();
			}
			vDevice.addCmdListener(new CmdListener() {
				@Override
				public void excute(Map<String, String> cmd, NotifyResponse resp) {
					if ((req = cmd.get("switch")) != null) {
						if (on.equals(req)) {
							LOG.info(">> Recieved [1] : TURN ON THE SWITCH");
							try {
								rt.exec("gpio write 0 1");
							} catch (IOException e) {
								e.printStackTrace();
								LOG.warn(">>>> [1] gpio error");
							}
						} else if (off.equals(req)) {
							LOG.info(">> Recieved [0] : TURN OFF THE SWITCH");
							try {
								rt.exec("gpio write 0 0");
							} catch (IOException e) {
								e.printStackTrace();
								LOG.warn(">>>> [0] gpio error");
							}
						} else {
							LOG.warn(">> : Recieved : 1 or 0 not exists");
						}

						vDevice.putContent("controller-switch", "text/plain", "" + req);
					} else {
						LOG.error(">> Recived [null]");
					}
				}
			});
		} else {
			throw new Exception(">> 등록이 되어 있지 않음");
		}
	}

	public static void main(String[] args) throws Exception {

		SmartPlug smartPlug = new SmartPlug();

		smartPlug.register();
		smartPlug.listen();

	}
}
