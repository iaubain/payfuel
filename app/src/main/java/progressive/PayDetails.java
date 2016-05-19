package progressive;

/**
 * Created by Owner on 5/19/2016.
 */
public class PayDetails {
    private String payId;
    private String tel;
    private String voucher;
    private String authorCode;
    private String authentCode;

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public String getAuthorCode() {
        return authorCode;
    }

    public void setAuthorCode(String authorCode) {
        this.authorCode = authorCode;
    }

    public String getAuthentCode() {
        return authentCode;
    }

    public void setAuthentCode(String authentCode) {
        this.authentCode = authentCode;
    }
}
