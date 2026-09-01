package com.example.foodhub_android.ui.features.payout

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.*
import com.example.foodhub_android.data.remote.*
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class PayoutViewModel @Inject constructor(private val api: FoodApi): ViewModel() {
    val state = MutableStateFlow<PayoutOverview?>(null); val busy = MutableStateFlow(false); val loading = MutableStateFlow(true); val error = MutableStateFlow<String?>(null); val events = MutableSharedFlow<String>()
    init { refresh() }
    fun refresh()=viewModelScope.launch {
        loading.value=true; error.value=null
        when(val r=safeApiCall{api.getPayouts()}){
            is ApiResponse.Success->state.value=r.data
            is ApiResponse.Error->error.value=r.message?:"Unable to load payouts"
            is ApiResponse.Exception->error.value="Couldn’t connect to the payout service"
        }
        loading.value=false
    }
    fun saveBank(bank:String,name:String,number:String)=viewModelScope.launch { busy.value=true; when(val r=safeApiCall{api.savePayoutAccount(SavePayoutAccountRequest(bank,name,number))}){is ApiResponse.Success->{events.emit("Bank account saved");refresh()};is ApiResponse.Error->events.emit(r.message?:"Unable to save account");is ApiResponse.Exception->events.emit("Check your connection")};busy.value=false }
    fun request(amount:Double)=viewModelScope.launch { busy.value=true; when(val r=safeApiCall{api.requestPayout(RequestPayout(amount))}){is ApiResponse.Success->{events.emit("Payout requested");refresh()};is ApiResponse.Error->events.emit(r.message?:"Unable to request payout");is ApiResponse.Exception->events.emit("Check your connection")};busy.value=false }
}

@Composable fun PayoutScreen(navController: NavController, vm:PayoutViewModel= hiltViewModel()) {
    val data by vm.state.collectAsStateWithLifecycle(); val busy by vm.busy.collectAsStateWithLifecycle(); val loading by vm.loading.collectAsStateWithLifecycle(); val error by vm.error.collectAsStateWithLifecycle(); val context=LocalContext.current
    var bankDialog by remember{mutableStateOf(false)}; var payoutDialog by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){vm.events.collect{Toast.makeText(context,it,Toast.LENGTH_SHORT).show()}}
    FoodHubPage { FoodHubHeader("Bank and payouts","Transfers and payment history",onBack=navController::popBackStack)
        if(loading && data==null) StatePane("Loading payouts","Your secure payout details are loading",Icons.Rounded.AccountBalance,loading=true)
        else if(error!=null && data==null) StatePane("Payouts unavailable",error!!,Icons.Rounded.AccountBalance,actionLabel="Try again",onAction=vm::refresh)
        else LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item { ElevatedCard{Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Available",style=MaterialTheme.typography.labelLarge);Text(StringUtils.formatCurrency(data!!.availableBalance),style=MaterialTheme.typography.headlineMedium);Text(data!!.account?.let{"${it.bankName} · ${it.accountNumberMasked}"}?:"No bank account added");OutlinedButton({bankDialog=true},Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(if(data!!.account==null)"Add bank account" else "Change bank account")};Button({payoutDialog=true},enabled=data!!.account!=null&&data!!.availableBalance>0&&!busy,modifier=Modifier.fillMaxWidth().height(56.dp)){Text("Request payout")}}} }
            item { Text("Payout history",style=MaterialTheme.typography.titleLarge) }
            if(data!!.history.isEmpty()) item{Text("No payout requests yet",color=MaterialTheme.colorScheme.onSurfaceVariant)}
            items(data!!.history,key={it.id}){p->ElevatedCard{Row(Modifier.fillMaxWidth().padding(16.dp)){Column(Modifier.weight(1f)){Text(StringUtils.formatCurrency(p.amount),style=MaterialTheme.typography.titleMedium);Text(p.requestedAt.replace('T',' ').take(16),style=MaterialTheme.typography.bodySmall)};StatusPill(p.status)}}}
        }
    }
    if(bankDialog) BankDialog({bankDialog=false}){b,n,a->vm.saveBank(b,n,a);bankDialog=false}
    if(payoutDialog) AmountDialog(data!!.availableBalance,{payoutDialog=false}){vm.request(it);payoutDialog=false}
}
@Composable private fun BankDialog(close:()->Unit,save:(String,String,String)->Unit){var b by remember{mutableStateOf("")};var n by remember{mutableStateOf("")};var a by remember{mutableStateOf("")};AlertDialog(close,title={Text("Payout bank account")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(b,{b=it},label={Text("Bank name")});OutlinedTextField(n,{n=it},label={Text("Account holder")});OutlinedTextField(a,{a=it.filter(Char::isDigit)},label={Text("Account number")})}},confirmButton={Button({save(b,n,a)},enabled=b.length>=2&&n.length>=2&&a.length>=6){Text("Save")}},dismissButton={TextButton(close){Text("Cancel")}})}
@Composable private fun AmountDialog(max:Double,close:()->Unit,save:(Double)->Unit){var value by remember{mutableStateOf("")};val amount=value.toDoubleOrNull();AlertDialog(close,title={Text("Request payout")},text={Column{Text("Available ${StringUtils.formatCurrency(max)}");OutlinedTextField(value,{value=it.filter{c->c.isDigit()||c=='.'}},label={Text("Amount")})}},confirmButton={Button({save(amount!!)},enabled=amount!=null&&amount>0&&amount<=max){Text("Request")}},dismissButton={TextButton(close){Text("Cancel")}})}
