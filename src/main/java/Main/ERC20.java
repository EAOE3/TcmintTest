package Main;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class ERC20 {

    public static BigInteger getBalance(String contractAddress, String accountAddress) throws Exception {
        Function function = new Function(
                "balanceOf",
                Arrays.<Type>asList(new Address(accountAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));

        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = Settings.web3j.ethCall(
                        Transaction.createEthCallTransaction(accountAddress, contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        List<Type> someTypes = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());

        return (BigInteger) someTypes.get(0).getValue();
    }

}
