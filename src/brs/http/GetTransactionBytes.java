package brs.http;

import brs.Burst;
import brs.Transaction;
import brs.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static brs.http.JSONResponses.*;

public final class GetTransactionBytes extends APIServlet.APIRequestHandler {

  static final GetTransactionBytes instance = new GetTransactionBytes();

  private GetTransactionBytes() {
    super(new APITag[] {APITag.TRANSACTIONS}, "transaction");
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) {

    String transactionValue = req.getParameter("transaction");
    if (transactionValue == null) {
      return MISSING_TRANSACTION;
    }

    long transactionId;
    Transaction transaction;
    try {
      transactionId = Convert.parseUnsignedLong(transactionValue);
    } catch (RuntimeException e) {
      return INCORRECT_TRANSACTION;
    }

    transaction = Burst.getBlockchain().getTransaction(transactionId);
    JSONObject response = new JSONObject();
    if (transaction == null) {
      transaction = Burst.getTransactionProcessor().getUnconfirmedTransaction(transactionId);
      if (transaction == null) {
        return UNKNOWN_TRANSACTION;
      }
    } else {
      response.put("confirmations", Burst.getBlockchain().getHeight() - transaction.getHeight());
    }
    response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
    response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
    return response;

  }

}
